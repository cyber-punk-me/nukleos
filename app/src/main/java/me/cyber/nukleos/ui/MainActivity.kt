package me.cyber.nukleos.ui

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.WindowManager
import me.cyber.nukleos.R
import me.cyber.nukleos.databinding.MainActivityBinding
import me.cyber.nukleos.navigation.NavigationContextActivity


class MainActivity : NavigationContextActivity() {
    private val mViewModel by lazy {
        MainActivityViewModel()
    }

    override fun onStart() {
        super.onStart()
        window?.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<MainActivityBinding>(this, R.layout.main_activity).apply {
            viewModel = mViewModel
            with(navigation) {
                enableAnimation(false)
                enableShiftingMode(false)
                enableItemShiftingMode(false)
                setTextVisibility(false)
                increaseIcon()
            }
        }
    }
}