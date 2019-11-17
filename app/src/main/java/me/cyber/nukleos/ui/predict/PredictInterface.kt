package me.cyber.nukleos.ui.predict

import me.cyber.nukleos.BasePresenter
import me.cyber.nukleos.BaseView
import me.cyber.nukleos.api.PredictResponse

interface PredictInterface {

    interface View : BaseView {
        fun showData(data: List<FloatArray>)
        fun startCharts(isRunning: Boolean)
        fun hideNoStreamingMessage()
        fun showNoStreamingMessage()
        fun notifyPredictEnabled(enabled: Boolean)
        fun notifyPredict(prediction: PredictResponse)
        fun notifyPredictError(error: Throwable)
        fun updateMotors(iMotor: Int, direction: Int, speed: Int)
    }

    abstract class Presenter(override val view: BaseView) : BasePresenter<BaseView>(view) {
        abstract fun onPredictSwitched(on: Boolean, predictOnline: Boolean)
    }
}