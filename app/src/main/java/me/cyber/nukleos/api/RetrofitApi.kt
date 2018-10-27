package me.cyber.nukleos.api

import android.util.Log
import com.google.gson.JsonObject
import io.reactivex.Single
import io.reactivex.SingleEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import me.cyber.nukleos.utils.gson
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import retrofit2.http.Headers
import java.io.File
import java.util.*

class RetrofitApi(private val mUrl: String) : IApi {

    private val mRetrofit by lazy {
        Retrofit.Builder()
                .baseUrl(mUrl)
                .client(OkHttpClient.Builder().addInterceptor(HttpLoggingInterceptor()
                        .apply { level = HttpLoggingInterceptor.Level.BODY }).build())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
    }

    private val mRequest by lazy { mRetrofit.create(IRetrofitRequests::class.java) }


    override fun <T> sendDirect(request: Any, data: String): Single<T> = Single.create({ emitter: SingleEmitter<T> ->
        ApiGraph.graph.find { it.requestClass == request::class.java }?.let { link ->
            mRequest.get(link.method, UUID.randomUUID(), data).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({ result ->
                val resData: Any? = null
                val error: ApiError? = null
                when {
                    (resData as? T) != null -> emitter.onSuccess(resData)
                    error != null -> emitter.onError(ServerThrowable(error))
                }
            }, onError)
        } ?: emitter.onError(Throwable(""))
    }).processError()

    //todo use this onError and custom in api handling
    private val onError: (Throwable) -> Unit = { Log.d("---", "Api error $it") }


    fun postData(dataId: UUID, rawData: String, extension: String): Single<Meta> = Single.create { emitter: SingleEmitter<Meta> ->
        val data = RequestBody.create(MediaType.parse("text/plain"), rawData)
        mRequest.postData("data", dataId, data, extension).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe({ emitter.onSuccess(it) }
                        , { emitter.onError(it) })
    }

    private class ServerThrowable(val error: ApiError) : Throwable()

    private interface IRetrofitRequests {
        @POST("/{path}/{uuid}")
        @Headers("Content-Type: plain/text")
        fun get(@Path("path") path: String, @Path("uuid") uuid: UUID, @Body body: String): Single<ApiResponse>

        @POST("/{path}/{uuid}")
        @Headers("Content-Type: plain/text")
        fun postData(@Path("path") path: String, @Path("uuid") uuid: UUID, @Body body: RequestBody, @Query("ext") extension: String): Single<Meta>
    }

    private fun <T> Single<T>.processError(): Single<T> = this.retryWhen { errors ->
        errors.flatMapSingle { error ->
            when ((error as? ServerThrowable)?.error?.code) {
                else -> Single.error<Throwable>(error)
            }
        }
    }
}
