package me.cyber.nukleos.ui.predict

import me.cyber.nukleos.BasePresenter
import me.cyber.nukleos.BaseView
import me.cyber.nukleos.api.PredictResponse

interface PredictInterface {

    interface View : BaseView {
        fun showData(data: FloatArray)
        fun startCharts(isRunning: Boolean)
        fun hideNoStreamingMessage()
        fun showNoStreamingMessage()
        fun notifyPredict(prediction: PredictResponse)
        fun notifyPredictError(error: Throwable)
    }

    abstract class Presenter(override val view: BaseView) : BasePresenter<BaseView>(view) {
        abstract fun onPredictSwitched(on: Boolean)
    }


}