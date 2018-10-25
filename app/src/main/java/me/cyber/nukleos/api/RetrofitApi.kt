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

    private val mFileRequest by lazy { mRetrofit.create(IFileUploadService::class.java) }

    override fun <T> sendDirect(request: Any): Single<T> = Single.create({ emitter: SingleEmitter<T> ->

        ApiGraph.graph.find { it.requestClass == request::class.java }?.let { link ->
            val r = gson.toJsonTree(request).asJsonObject
            mRequest.get(link.method, UUID.randomUUID(), r).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({ result ->
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

    override fun uploadProfilePhoto(file: File, uuid: UUID) {
        val reqFile = RequestBody.create(MediaType.parse("text/csv"), file)
        // Выполняем запрос
        mFileRequest.upload(MultipartBody.Part.createFormData("huiaad", file.name, reqFile), uuid)
                ?.enqueue(object : retrofit2.Callback<ResponseBody> {
                    override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {
                        Log.e("|||||||||||", "---------fail-----------${t?.message}------------------")
                    }

                    override fun onResponse(call: Call<ResponseBody>?, response: Response<ResponseBody>?) {
                        Log.e("|||||||||||", "-----------ok---------${response.toString()}------------------")
                    }
                })
    }

}

private class ServerThrowable(val error: ApiError) : Throwable()

private interface IRetrofitRequests {
    @POST("/{method}/{uuid}/")
    fun get(@Path("method") method: String, @Path("uuid") uuid: UUID, @Body body: JsonObject): Single<ApiResponse>
}

private interface IFileUploadService {
    @Multipart
    @POST("/data/{uuid}/")
    fun upload(@Part file: MultipartBody.Part, @Path("uuid") uuid: UUID): Call<ResponseBody>
}

private fun <T> Single<T>.processError(): Single<T> = this.retryWhen { errors ->
    errors.flatMapSingle { error ->
        when ((error as? ServerThrowable)?.error?.code) {
            else -> Single.error<Throwable>(error)
        }
    }
}
