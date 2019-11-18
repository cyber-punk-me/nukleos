package me.cyber.nukleos.ui.predict

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.ViewGroup
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.layout_predict.*
import me.cyber.nukleos.BaseFragment
import me.cyber.nukleos.api.PredictResponse
import me.cyber.nukleos.sensors.myosensor.MYO_CHANNELS
import me.cyber.nukleos.sensors.myosensor.MYO_MAX_VALUE
import me.cyber.nukleos.sensors.myosensor.MYO_MIN_VALUE
import me.cyber.nukleos.utils.showShortToast
import me.cyber.nukleus.R
import javax.inject.Inject

class PredictFragment : BaseFragment<PredictInterface.Presenter>(), PredictInterface.View {


    companion object {
        fun newInstance() = PredictFragment()
    }

    @Inject
    lateinit var predictPresenter: PredictPresenter

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        attachPresenter(predictPresenter)
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
            inflater.inflate(R.layout.layout_predict, container, false).apply { setHasOptionsMenu(true) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(sensor_charts_predict_view) {
            mChartsCount = MYO_CHANNELS
            maxValue = MYO_MAX_VALUE
            minValue = MYO_MIN_VALUE
        }
        predict_toggle.setOnClickListener { predictPresenter.onPredictSwitched(predict_toggle.isChecked, predict_online_toggle.isChecked) }
        predict_online_toggle.setOnClickListener { predictPresenter.onPredictSwitched(predict_toggle.isChecked, predict_online_toggle.isChecked) }
        button_action1.visibility = INVISIBLE
        button_action2.visibility = INVISIBLE
        button_action3.visibility = INVISIBLE
        button_action4.visibility = INVISIBLE
    }

    override fun showData(data: List<FloatArray>) {
        activity?.runOnUiThread {
            data.forEach {
                sensor_charts_predict_view?.addNewPoint(it)
            }
        }
    }

    override fun updateMotors(iMotor: Int, direction: Int, speed: Int) = "motor $iMotor moving $direction at $speed".showShortToast()

    override fun notifyPredict(response: PredictResponse) {
        val prediction = response.predictions[0]
        val max = prediction.distr.max()
        val min = prediction.distr.min()
        val delta = max!! - min!!

        val normalized = prediction.distr.map { ((it - min) / (delta) * 100).toInt().toString() }
        .map { it.padStart(3, ' ')}

        activity?.runOnUiThread {
            sensor_notification.text = "${prediction.output} | $normalized"
        }
    }

    override fun notifyPredictEnabled(enabled: Boolean) {
        if (!enabled) {
            activity?.runOnUiThread {
                sensor_notification.text = ""
                if (predict_toggle.isChecked) {
                    predict_toggle.isChecked = false
                }
            }
        }
    }

    override fun notifyPredictError(error: Throwable) = (error.message ?: "").showShortToast()

    override fun startCharts(isRunning: Boolean) {
        sensor_charts_predict_view?.apply {
            this.isRunning = isRunning
        }
    }

    override fun showNoStreamingMessage() {
        sensor_notification.text = getText(R.string.connect_data_source)
    }

    override fun hideNoStreamingMessage() {
        sensor_notification.text = ""
    }
}