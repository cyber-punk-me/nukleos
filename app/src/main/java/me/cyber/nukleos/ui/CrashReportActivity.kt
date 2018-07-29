package me.cyber.nukleos.ui

import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import me.cyber.nukleos.R
import net.hockeyapp.android.CrashManager
import net.hockeyapp.android.CrashManager.register

open class CrashReportActivity : AppCompatActivity() {

    override fun onResume() {
        super.onResume()
        checkForCrashes()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        register(application)
    }

    private fun checkForCrashes() = CrashManager.register(this, getAppId())

    private fun getAppId(): String? {
        val activityInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
        return activityInfo.metaData?.getString(getString(R.string.hockey_app_meta_data_name))
    }
}