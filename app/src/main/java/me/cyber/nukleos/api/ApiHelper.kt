package me.cyber.nukleos.api

class ApiHelper {
    companion object {
        private const val DEFAULT_LOCAL_URL = "http://192.168.1.48:8080"
        private const val DEFAULT_API_URL = "http://192.168.1.48:8080"
    }

    fun getApiUrl() = DEFAULT_LOCAL_URL
    val api: RetrofitApi by lazy { RetrofitApi(getApiUrl()) }
}