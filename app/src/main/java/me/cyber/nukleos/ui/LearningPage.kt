package me.cyber.nukleos.ui

import android.Manifest
import android.databinding.DataBindingUtil
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import me.cyber.nukleos.R
import me.cyber.nukleos.databinding.LearningLayoutBinding

class LearningPage : Fragment() {

    companion object {
        fun newInstance() = LearningPage()
        private const val PERMISSION_REQUEST_COARSE_LOCATION = 456
    }

    private val mBinding by lazy {
        DataBindingUtil.inflate<LearningLayoutBinding>(layoutInflater,
                R.layout.learning_layout, null, false)
    }

    private val mViewModel by lazy {
        LearningViewModel()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
            mBinding.apply {
                onCreate(savedInstanceState)
                viewModel = mViewModel
            }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),PERMISSION_REQUEST_COARSE_LOCATION)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mViewModel.release()
    }
}
