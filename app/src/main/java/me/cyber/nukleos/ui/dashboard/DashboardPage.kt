package me.cyber.nukleos.ui.dashboard

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import me.cyber.nukleos.R
import me.cyber.nukleos.databinding.DashboardLayoutBinding

class DashboardPage : Fragment() {

    companion object {
        fun newInstance() = DashboardPage()
    }

    private val mBinding by lazy {
        DataBindingUtil.inflate<DashboardLayoutBinding>(layoutInflater,
                R.layout.dashboard_layout, null, false)
    }

    private val mViewModel by lazy {
        DashboardViewModel()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
            mBinding.apply {
                onCreate(savedInstanceState)
                viewModel = mViewModel
            }.root

    override fun onDestroy() {
        super.onDestroy()
        mViewModel.release()
    }
}
