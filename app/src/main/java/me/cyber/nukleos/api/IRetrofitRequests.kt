package me.cyber.nukleos.api

import io.reactivex.Single
import okhttp3.RequestBody
import retrofit2.http.*
import java.util.*

interface IRetrofitRequests {

    @POST("/data/{dataId}")
    @Headers("Content-Type: plain/text")
    fun postData(@Path("dataId") dataId: UUID,
                 @Body body: RequestBody,
                 @Query("ext") extension: String): Single<Meta>

    @POST("/model/eae583bd-bc4b-4d3b-8fb8-11ac76d18b1e?gpu=true")
    fun postModel(@Body body: Model): Single<Meta>

    @POST("/apply/eae583bd-bc4b-4d3b-8fb8-11ac76d18b1e")
    fun predict(@Body body: PredictRequest): Single<PredictResponse>

}