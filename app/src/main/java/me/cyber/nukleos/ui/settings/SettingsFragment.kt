package me.cyber.nukleos.ui.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.preference.PreferenceFragmentCompat
import android.util.Log
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import me.cyber.nukleos.App
import me.cyber.nukleos.utils.showShortToast
import me.cyber.nukleus.R
import java.util.*

class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    private var checkPrefsSubscrube: Disposable? = null

    companion object {
        fun newInstance() = SettingsFragment()
        val serverAddress = "server_address"
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    override fun onResume() {
        super.onResume()

        val sharedPreferences = preferenceManager.sharedPreferences

        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        for (entry in sharedPreferences.all.entries) {
            updateSummary(entry.key)
        }
    }

    override fun onPause() {
        preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        super.onPause()
        checkPrefsSubscrube?.dispose()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences,
                                           key: String) {
        updateSummary(key)
        when(key) {
            serverAddress -> checkConnection()
        }
    }

    private fun checkConnection() {
        checkPrefsSubscrube = App.applicationComponent.getApiHelper().getApi().getServerTime()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    "Server time : ${Date(it)}.".showShortToast()
                }, {
                    "Server ${preferenceManager.sharedPreferences.getString(serverAddress, "")} not connected.".showShortToast()
                })
    }

    private fun updateSummary(key: String) {

        val sharedPreferences = preferenceManager.sharedPreferences
        val preference = findPreference(key) ?: return
        preference.summary = sharedPreferences.getString(key, "")
    }

}
