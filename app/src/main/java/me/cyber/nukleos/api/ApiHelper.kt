package me.cyber.nukleos.api

class ApiHelper {
    companion object {
        private const val DEFAULT_LOCAL_URL = "http://192.168.1.42:8080"
        private const val DEFAULT_API_URL_MIR = "http://192.168.100.4:8080"
    }

    fun getApiUrl() = DEFAULT_LOCAL_URL

    val api: RetrofitApi by lazy { RetrofitApi(getApiUrl()) }
}