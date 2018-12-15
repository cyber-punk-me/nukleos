package me.cyber.nukleos.ui.control

import me.cyber.nukleos.BasePresenter
import me.cyber.nukleos.BaseView


interface SensorControlInterface {

    interface View : BaseView {

        fun showSensorStuffInformation(name: String?, address: String)
        fun showConnectionLoader()
        fun hideConnectionLoader()
        fun showConnected()
        fun showConnecting()
        fun showDisconnected()
        fun showConnectionError()
        fun disableConnectButton()
        fun enableConnectButton()
        fun showScan()
        fun showNotScan()
        fun enableControlPanel()
        fun disableControlPanel()
        fun showScanFrequency(frequency: Int)
    }

    abstract class Presenter(override val view: BaseView) : BasePresenter<BaseView>(view) {

        abstract fun onConnectionButtonClicked()
        abstract fun onStartButtonClicked()
        abstract fun onVibrationClicked(vibrationDuration: Int)
        abstract fun onProgressSelected(progress: Int)

    }
}