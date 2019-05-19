package me.cyber.nukleos.ui.control

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import me.cyber.nukleos.bluetooth.BluetoothConnector
import me.cyber.nukleos.dagger.PeripheryManager
import me.cyber.nukleos.sensors.Status

class SensorStuffPresenter(override val view: SensorControlInterface.View, private val mBluetoothConnector: BluetoothConnector,
                           private val mPeripheryManager: PeripheryManager) : SensorControlInterface.Presenter(view) {

    private var mSensorStatusSubscription: Disposable? = null
    private var mSensorControlSubscription: Disposable? = null

    override fun create() {}

    override fun start() {
        with(view) {
            val selectedSensor = mPeripheryManager.getLastSelectedSensor()
            if (selectedSensor == null) {
                disableConnectButton()
                return
            }

            showSensorStuffInformation(selectedSensor.name, selectedSensor.address)
            enableConnectButton()

            selectedSensor.apply {
                mSensorStatusSubscription =
                        this.statusObservable()
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe {
                                    when (it) {
                                        Status.CONNECTING -> {
                                            showConnectionLoader()
                                            showConnecting()
                                            showNotScan()
                                        }
                                        Status.STREAMING -> {
                                            hideConnectionLoader()
                                            showConnected()
                                            showScan()
                                        }
                                        else -> {
                                            hideConnectionLoader()
                                            showDisconnected()
                                            disableControlPanel()
                                            showNotScan()
                                        }
                                    }
                                }
            }
        }
    }

    override fun destroy() {
        mSensorStatusSubscription?.dispose()
        mSensorControlSubscription?.dispose()
    }

    override fun onConnectionButtonClicked() {
        mPeripheryManager.getLastSelectedSensor()?.apply {
            if (!isConnected()) {
                connect()
            } else {
                disconnect()
            }
        }
    }

    override fun onProgressSelected(progress: Int) {
        val sensor = mPeripheryManager.getLastSelectedSensor() ?: return
        val availableFrequencies = sensor.getAvailableFrequencies()
        val selectedFrequency = if (progress >= 0 && progress < availableFrequencies.size)
            availableFrequencies[progress]
        else
            availableFrequencies.last()
        sensor.setFrequency(selectedFrequency)
        view.showScanFrequency(selectedFrequency)
    }

    override fun onVibrationClicked(vibrationDuration: Int) {
        mPeripheryManager.getLastSelectedSensor()?.apply {
            vibration(vibrationDuration)
        }
    }
}