package me.cyber.nukleos.ui.charts

import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.layout_charts.*
import me.cyber.nukleos.BaseFragment
import me.cyber.nukleos.sensors.myosensor.MYO_CHANNELS
import me.cyber.nukleos.sensors.myosensor.MYO_MAX_VALUE
import me.cyber.nukleos.sensors.myosensor.MYO_MIN_VALUE
import me.cyber.nukleos.ui.MainActivity
import me.cyber.nukleos.utils.safeToInt
import me.cyber.nukleos.utils.showShortToast
import me.cyber.nukleus.R
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ChartsFragment : BaseFragment<ChartInterface.Presenter>(), ChartInterface.View {

    companion object {

        private const val TIMER_FORMAT = "%02d:%02d:%02d"
        const val TIMER_COUNT = 5L
        const val LEARNING_TIME = 10

        fun newInstance() = ChartsFragment()
    }

    @Inject
    lateinit var graphPresenter: ChartsPresenter

    private var mDataType = ""

    private fun blockNavigation(blocked: Boolean) = (activity as MainActivity).blockNavigaion(blocked)
    private fun showCountdown() {
        object : CountDownTimer(TIMER_COUNT * 1000, 1000) {
            override fun onFinish() {
                graphPresenter.onCountdownFinished()
            }

            override fun onTick(millisUntilFinished: Long) {
                val minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
                val hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
                countdown_text.text = String.format(TIMER_FORMAT,
                        hours,
                        minutes - TimeUnit.HOURS.toMinutes(hours),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(minutes))
            }
        }.apply { countdown_layout.visibility = View.VISIBLE }.start()
    }

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
        data_type_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                getResources().getStringArray(R.array.dataTypes).apply {
                    record_button.setText(R.string.record_data)
                    mDataType = get(position)
                }
            }

        }
        record_button.setOnClickListener { graphPresenter.onCollectPressed() }
        train_button.setOnClickListener { graphPresenter.onTrainPressed() }
        train_button.isEnabled = false

        calibrate_button.setOnClickListener { graphPresenter.onCalibratePressed() }
    }

    override fun notifyTrainModelStarted() = "Model training started.".showShortToast()
    override fun notifyTrainModelFailed() = "Model training failed.".showShortToast()
    override fun notifyDataSent() = "Data sent.".showShortToast()
    override fun notifyDataFailed() = "Data transfer failed.".showShortToast()

    override fun showData(data: List<FloatArray>) {
        activity?.runOnUiThread {
            data.forEach {
                sensor_charts_view?.addNewPoint(it)
            }
        }
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

    override fun getDataType() = mDataType.safeToInt(-1)

    override fun goToState(state: ChartInterface.State) {
        when (state) {
            ChartInterface.State.IDLE -> {
                countdown_layout.visibility = View.INVISIBLE
                record_button.isEnabled = true
                data_type_spinner.isEnabled = true
                startCharts(true)
                train_button.isEnabled = true
                calibrate_button.isEnabled = true
                blockNavigation(false)
            }
            ChartInterface.State.COUNTDOWN -> {
                record_button.isEnabled = false
                data_type_spinner.isEnabled = false
                showCountdown()
                startCharts(false)
                train_button.isEnabled = false
                calibrate_button.isEnabled = false
                blockNavigation(true)
            }
            ChartInterface.State.RECORDING -> {
                record_button.isEnabled = false
                data_type_spinner.isEnabled = false
                countdown_layout.visibility = View.INVISIBLE
                startCharts(true)
                train_button.isEnabled = false
                calibrate_button.isEnabled = false
                blockNavigation(true)
            }
            ChartInterface.State.SENDING -> {
                record_button.isEnabled = false
                data_type_spinner.isEnabled = false
                countdown_layout.visibility = View.INVISIBLE
                startCharts(false)
                train_button.isEnabled = false
                calibrate_button.isEnabled = false
                blockNavigation(true)
            }
        }
    }

    override fun setDataType(selectedType: Int) {
        data_type_spinner.setSelection(selectedType)
    }
}
