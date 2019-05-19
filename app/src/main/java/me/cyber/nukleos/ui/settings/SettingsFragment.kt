package me.cyber.nukleos.ui.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.preference.PreferenceFragmentCompat
import me.cyber.nukleus.R

class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    companion object {
        fun newInstance() = SettingsFragment()
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
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences,
                                           key: String) {
        updateSummary(key)
    }

    private fun updateSummary(key: String) {

        val sharedPreferences = preferenceManager.sharedPreferences
        val preference = findPreference(key) ?: return
        preference.summary = sharedPreferences.getString(key, "")
    }

}
