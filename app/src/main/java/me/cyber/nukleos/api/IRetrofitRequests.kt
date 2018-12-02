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

    @POST("/model/1d722019-c892-44bc-844b-eb5708d55987")
    fun postModel(@Body body: Model): Single<Meta>

    @POST("/apply/1d722019-c892-44bc-844b-eb5708d55987")
    fun predict(@Body body: PredictRequest): Single<PredictResponse>

}