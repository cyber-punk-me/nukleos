package me.cyber.nukleos.api

import android.content.SharedPreferences
import android.preference.PreferenceManager
import io.reactivex.android.schedulers.AndroidSchedulers
import me.cyber.nukleos.App
import me.cyber.nukleus.R

class ApiHelper : SharedPreferences.OnSharedPreferenceChangeListener {

    companion object {
        private const val DEFAULT_LOCAL_URL = "http://192.168.1.48:8080"
    }

    private var apiLazy = lazy { RetrofitApi(getApiUrl(), AndroidSchedulers.mainThread()) }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == App.applicationComponent.getAppContext().getString(R.string.server_address_key)) {
            resetRetrofitApi()
        }
    }

    private fun getApiUrl(): String {
        val context = App.applicationComponent.getAppContext()
        return PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.server_address_key), DEFAULT_LOCAL_URL)
                ?: DEFAULT_LOCAL_URL
    }


    private fun resetRetrofitApi() {
        apiLazy = lazy { RetrofitApi(getApiUrl(), AndroidSchedulers.mainThread()) }
    }

    fun getApi(): RetrofitApi = apiLazy.value
}