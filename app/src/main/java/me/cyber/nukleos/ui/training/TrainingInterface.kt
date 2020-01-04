package me.cyber.nukleos.ui.training

import me.cyber.nukleos.BasePresenter
import me.cyber.nukleos.BaseView
import me.cyber.nukleos.api.ModelMeta

interface TrainingInterface {

    enum class State {
        IDLE, COUNTDOWN, RECORDING, SENDING
    }

    interface View : BaseView {
        fun showData(data: List<FloatArray>)
        fun startCharts(isRunning: Boolean)
        fun hideNoStreamingMessage()
        fun showNoStreamingMessage()
        //todo state params
        fun goToState(state: State, dataWindow: Int? = null,
                      onDataCollected: ((List<FloatArray>) -> Unit)? = {})
        fun getDataType(): Int
        fun setDataType(selectedType: Int)
        fun notifyDataSent()
        fun notifyDataFailed()
        fun notifyTrainModelStarted(modelMeta: ModelMeta)
        fun notifyTrainModelFailed()
    }

    abstract class Presenter(override val view: BaseView) : BasePresenter<BaseView>(view) {
        abstract fun onCollectPressed()
        abstract fun onCollectStarted(dataWindow: Int, onCollected: (List<FloatArray>) -> Unit)
        abstract fun onTrainPressed()
        abstract fun onCalibratePressed()

    }
}