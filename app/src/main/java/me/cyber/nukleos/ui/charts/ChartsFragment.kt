package me.cyber.nukleos.ui.charts

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.layout_charts.*
import me.cyber.nukleos.BaseFragment
import me.cyber.nukleos.myosensor.MYO_CHANNELS
import me.cyber.nukleos.myosensor.MYO_MAX_VALUE
import me.cyber.nukleos.myosensor.MYO_MIN_VALUE
import me.cyber.nukleus.R
import javax.inject.Inject

class ChartsFragment : BaseFragment<ChartInterface.Presenter>(), ChartInterface.View {

    companion object {
        fun newInstance() = ChartsFragment()
    }

    @Inject
    lateinit var graphPresenter: ChartsPresenter

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        attachPresenter(graphPresenter)
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
            inflater.inflate(R.layout.layout_charts, container, false).apply { setHasOptionsMenu(true) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(sensor_charts_view) {
            mChartsCount = MYO_CHANNELS
            maxValue = MYO_MAX_VALUE
            minValue = MYO_MIN_VALUE
        }
    }

    override fun showData(data: FloatArray) {
        sensor_charts_view?.addNewPoint(data)
    }

    override fun startCharts(isRunning: Boolean) {
        sensor_charts_view?.apply {
            this.isRunning = isRunning
        }
    }

    override fun showNoStreamingMessage() {
        empty_chat_text.visibility = View.VISIBLE
    }

    override fun hideNoStreamingMessage() {
        empty_chat_text.visibility = View.INVISIBLE
    }
}