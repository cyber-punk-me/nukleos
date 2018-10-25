package me.cyber.nukleos.api

data class ApiLink(val method: String, val requestClass: Class<*>, val responseClass: Class<*>)

object ApiGraph {
    val graph = listOf(
            ApiLink("data", DataRequest::class.java, CompleteResponse::class.java)
    )
}