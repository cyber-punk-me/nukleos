package me.cyber.nukleos.ui.charts

import me.cyber.nukleos.BasePresenter
import me.cyber.nukleos.BaseView


interface ChartInterface {

    interface View : BaseView {
        fun showData(data: FloatArray)
        fun startCharts(isRunning: Boolean)
        fun hideNoStreamingMessage()
        fun showNoStreamingMessage()
    }

    abstract class Presenter(override val view: BaseView) : BasePresenter<BaseView>(view)
}