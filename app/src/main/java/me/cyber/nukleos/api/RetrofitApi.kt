package me.cyber.nukleos.api

import io.reactivex.Single
import io.reactivex.SingleEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import java.util.concurrent.TimeUnit

class RetrofitApi(private val mUrl: String) {

    private val mRetrofit by lazy {
        Retrofit.Builder()
                .baseUrl(mUrl)
                .client(OkHttpClient.Builder().addInterceptor(HttpLoggingInterceptor()
                        .apply { level = HttpLoggingInterceptor.Level.BODY })
                        .connectTimeout(1, TimeUnit.SECONDS)
                        .readTimeout(1, TimeUnit.SECONDS)
                        .writeTimeout(1, TimeUnit.SECONDS)
                        .build())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
    }

    private val mRequest by lazy { mRetrofit.create(IRetrofitRequests::class.java) }

    fun postData(dataId: UUID, rawData: String, extension: String) = Single.create { emitter: SingleEmitter<Meta> ->
        mRequest.postData(dataId, RequestBody.create(MediaType.parse("text/plain"), rawData), extension)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    emitter.onSuccess(it)
                }, {
                    emitter.onError(it)
                })
    }

    fun trainModel(dataId: UUID, scriptId: UUID) = Single.create { emitter: SingleEmitter<Meta> ->
        mRequest.postModel(Model(dataId, scriptId))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    emitter.onSuccess(it)
                }, {
                    emitter.onError(it)
                })
    }

    fun predict(data: PredictRequest) = Single.create { emitter: SingleEmitter<PredictResponse> ->
        mRequest.predict(data)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    emitter.onSuccess(it)
                }, {
                    emitter.onError(it)
                })
    }

    fun getServerTime() = Single.create{ emitter : SingleEmitter<Long> ->
        mRequest.getServerTime()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    emitter.onSuccess(it)
                }, {
                    emitter.onError(it)
                })
    }

}
