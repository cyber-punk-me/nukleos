package me.cyber.nukleos.api

import io.reactivex.Single

interface IApi {
    fun <T> sendDirect(request: Any): Single<T>
}