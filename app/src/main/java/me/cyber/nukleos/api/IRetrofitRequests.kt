package me.cyber.nukleos.api

import io.reactivex.Single
import okhttp3.RequestBody
import retrofit2.http.*
import java.util.*

interface IRetrofitRequests {
    @POST("/{path}/{uuid}")
    @Headers("Content-Type: plain/text")
    fun postData(@Path("path") path: String,
                 @Path("uuid") uuid: UUID,
                 @Body body: RequestBody,
                 @Query("ext") extension: String): Single<Meta>
}