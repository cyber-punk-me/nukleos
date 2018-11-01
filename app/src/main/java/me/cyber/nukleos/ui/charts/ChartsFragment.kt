package me.cyber.nukleos.ui.charts

import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.layout_charts.*
import me.cyber.nukleos.BaseFragment
import me.cyber.nukleos.myosensor.MYO_CHANNELS
import me.cyber.nukleos.myosensor.MYO_MAX_VALUE
import me.cyber.nukleos.myosensor.MYO_MIN_VALUE
import me.cyber.nukleos.utils.safeToInt
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
                    collecting.setText(R.string.start_collecting)
                    button_send.isEnabled = false
                    mDataType = get(position)
                }
            }

        }
        collecting.setOnClickListener { graphPresenter.onCollectPressed() }
        button_send.setOnClickListener { graphPresenter.onSavePressed() }
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

    override fun getDataType() = mDataType.safeToInt(-1)

    override fun readyForSending() {
        collecting.setText(R.string.get_new_data)
        button_send.isEnabled = true
    }

    override fun newCollecting() {}

    override fun showNotStreamingErrorMessage() {
        Toast.makeText(activity, "Please, connect sensor and start scanning", Toast.LENGTH_SHORT).show()
    }

    override fun showCountdown() {
        object : CountDownTimer(TIMER_COUNT * 1000, 1000) {
            override fun onFinish() {
                countdown_text.text = "COLLECTING"
                countdown_title_text.text = ""
                progressbar.visibility = VISIBLE
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

    override fun learningIsFinish() {
        countdown_layout.visibility = View.GONE
        progressbar.visibility = VISIBLE
    }
}
