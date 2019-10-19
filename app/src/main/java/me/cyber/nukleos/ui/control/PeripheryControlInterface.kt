package me.cyber.nukleos.ui.control

import me.cyber.nukleos.BasePresenter
import me.cyber.nukleos.BaseView


interface PeripheryControlInterface {

    interface View : BaseView {

        fun showSensorStuffInformation(name: String?, address: String)
        fun showConnectionLoader()
        fun hideConnectionLoader()
        fun showSensorConnected()
        fun showSensorConnecting()
        fun showSensorDisconnected()
        fun showSensorConnectionError()
        fun disableSensorConnectButton()
        fun enableSensorConnectButton()
        fun showSensorStreaming()
        fun showSensorNotStreaming()
        fun enableSensorControlPanel()
        fun disableSensorControlPanel()
        fun showSensorScanFrequency(frequency: Int)

        fun showMotorsConnected()
        fun showMotorsConnecting()
        fun showMotorsDisonnected()


    }

    abstract class Presenter(override val view: BaseView) : BasePresenter<BaseView>(view) {

        abstract fun onConnectSensorClicked()
        abstract fun onVibrationClicked(vibrationDuration: Int)
        abstract fun onProgressSelected(progress: Int)

        abstract fun onConnectMotorsClicked()

    }
}