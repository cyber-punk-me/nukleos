package me.cyber.nukleos.api

class ApiHelper {
    companion object {
        private const val DEFAULT_LOCAL_URL = "http://localhost:8080"
        private const val DEFAULT_API_URL = "http://localhost:8080"
    }

    fun getApiUrl() = DEFAULT_LOCAL_URL
    val api: IApi by lazy { RetrofitApi(getApiUrl()) }
}