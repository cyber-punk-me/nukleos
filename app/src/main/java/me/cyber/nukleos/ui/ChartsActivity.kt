package me.cyber.nukleos.ui

import android.Manifest
import android.databinding.DataBindingUtil
import android.os.Build
import android.os.Bundle
import io.kyr.jarvis.R
import io.kyr.jarvis.databinding.ChartsActivityBinding

class ChartsActivity : CrashReportActivity() {

    companion object {
        private val PERMISSION_REQUEST_COARSE_LOCATION = 456
    }

    private val mViewModel = ChartsActivityViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<ChartsActivityBinding>(this, R.layout.charts_activity).apply {
            vm = mViewModel
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), PERMISSION_REQUEST_COARSE_LOCATION)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mViewModel.release()
    }

    override fun finish() {
        mViewModel.shutDown()
        super.finish()
    }
}
