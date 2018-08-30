package me.cyber.nukleos.api

class ApiHelper {
    companion object {
        private const val DEFAULT_API_URL = "https://virtserver.swaggerhub.com/kyr7/hivemind/1.0.0"
    }

    fun getApiUrl() = DEFAULT_API_URL
    val api: IApi by lazy { RetrofitApi(getApiUrl()) }
}