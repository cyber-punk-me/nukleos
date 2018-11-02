package me.cyber.nukleos.ui.charts

import me.cyber.nukleos.BasePresenter
import me.cyber.nukleos.BaseView

interface ChartInterface {

    enum class State {
        IDLE, COUNTDOWN, RECORDING, SENDING
    }

    interface View : BaseView {
        fun showData(data: FloatArray)
        fun startCharts(isRunning: Boolean)
        fun hideNoStreamingMessage()
        fun showNoStreamingMessage()

        fun goToState(state : State)
        fun getDataType(): Int
        fun notifyDataSent()
        fun notifyDataFailed()
        fun notifyTrainModelStarted()
        fun notifyTrainModelFailed()
    }

    abstract class Presenter(override val view: BaseView) : BasePresenter<BaseView>(view) {
        abstract fun onCollectPressed()
        abstract fun onTrainPressed()

    }
}