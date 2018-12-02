package me.cyber.nukleos.ui.predict

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.layout_predict.*
import me.cyber.nukleos.BaseFragment
import me.cyber.nukleos.api.PredictResponse
import me.cyber.nukleos.myosensor.MYO_CHANNELS
import me.cyber.nukleos.myosensor.MYO_MAX_VALUE
import me.cyber.nukleos.myosensor.MYO_MIN_VALUE
import me.cyber.nukleus.R
import javax.inject.Inject

class PredictFragment : BaseFragment<PredictInterface.Presenter>(), PredictInterface.View {

    override fun notifyPredict(response: PredictResponse) {
        val prediction = response.predictions[0]
        val max = prediction.distr.max()
        val min = prediction.distr.min()
        val delta = max!! - min!!

        val normalized = prediction.distr.map { it -> ((it - min) / (delta) * 100).toInt() }
        Toast.makeText(context, "${prediction.output} | $normalized", Toast.LENGTH_SHORT).show()
    }

    override fun notifyPredictError(error: Throwable) {
        Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
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
        predict_toggle.setOnClickListener { predictPresenter.onPredictSwitched(predict_toggle.isChecked) }
    }

    override fun showData(data: FloatArray) {
        sensor_charts_predict_view?.addNewPoint(data)
    }

    override fun startCharts(isRunning: Boolean) {
        sensor_charts_predict_view?.apply {
            this.isRunning = isRunning
        }
    }

    override fun showNoStreamingMessage() {
        connect_device_warning.visibility = View.VISIBLE
    }

    override fun hideNoStreamingMessage() {
        connect_device_warning.visibility = View.INVISIBLE
    }

    companion object {
        fun newInstance() = PredictFragment()
    }

}