package me.cyber.nukleos.api

import io.reactivex.Single
import okhttp3.RequestBody
import java.io.File
import java.util.*

interface IApi {
    fun <T> sendDirect(request: Any, data: String): Single<T>
}