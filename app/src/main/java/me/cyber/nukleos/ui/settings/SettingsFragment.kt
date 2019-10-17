package me.cyber.nukleos.ui.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceFragmentCompat
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
        const val serverAddress = "server_address"
        val defaultDuration = 1000
        val maxDuration = 1000
        val defaultIntensity = 127
        val maxIntensity = 127
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

        addActionLengthValidation(findPreference("action_1_length"), findPreference("action_2_length"),
                findPreference("action_3_length"), findPreference("action_4_length"))

        addActionIntensityValidation(findPreference("action_1_intensity"), findPreference("action_2_intensity"),
                findPreference("action_3_intensity"), findPreference("action_4_intensity"))
    }

    fun addActionLengthValidation(vararg prefs: Preference) {
        prefs.forEach {
            it.setOnPreferenceChangeListener { _, any ->
                try {
                    val value = Integer.parseInt(any.toString())
                    value in 1 .. maxDuration
                } catch (t : Throwable) {
                    false
                }
            }
        }
    }

    fun addActionIntensityValidation(vararg prefs: Preference) {
        prefs.forEach {
            it.setOnPreferenceChangeListener { _, any ->
                try {
                    val value = Integer.parseInt(any.toString())
                    value in 1 .. maxIntensity
                } catch (t: Throwable) {
                    false
                }
            }
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
        when (key) {
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
        when (key) {
            serverAddress -> preference.summary = sharedPreferences.getString(key, "")
            else -> {
                try {
                    preference.summary = "" + sharedPreferences.getString(key, "")
                } catch (t: Throwable) {
                    try {
                        preference.summary = "" + sharedPreferences.getBoolean(key, false)
                    } catch (t: Throwable) {
                    }
                }

            }
        }

    }

}
