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

class RetrofitApi(private val mUrl: String) {

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

    fun postData(dataId: UUID, rawData: String, extension: String): Single<Meta> = Single.create { emitter: SingleEmitter<Meta> ->
        val data = RequestBody.create(MediaType.parse("text/plain"), rawData)
        mRequest.postData(dataId, data, extension)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    emitter.onSuccess(it)
                }, {
                    emitter.onError(it)
                })
    }

    fun trainModel(dataId: UUID, scriptId: UUID): Single<Meta> = Single.create { emitter: SingleEmitter<Meta> ->
        val trainMeta = Model(dataId, scriptId)
        mRequest.postModel(trainMeta)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    emitter.onSuccess(it)
                }, {
                    emitter.onError(it)
                })
    }

    fun predict(data: PredictRequest): Single<PredictResponse> = Single.create { emitter: SingleEmitter<PredictResponse> ->
        mRequest.predict(data)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    emitter.onSuccess(it)
                }, {
                    emitter.onError(it)
                })
    }

}
