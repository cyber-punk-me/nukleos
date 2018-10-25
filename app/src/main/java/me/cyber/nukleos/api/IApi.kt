package me.cyber.nukleos.api

import io.reactivex.Single
import java.io.File
import java.util.*

interface IApi {
    fun <T> sendDirect(request: Any): Single<T>
    fun uploadProfilePhoto(file: File, uuid: UUID)
}