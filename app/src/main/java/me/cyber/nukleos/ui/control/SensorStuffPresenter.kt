package me.cyber.nukleos.ui.control

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import me.cyber.nukleos.bluetooth.BluetoothConnector
import me.cyber.nukleos.dagger.PeripheryManager
import me.cyber.nukleos.sensors.ControlStatus
import me.cyber.nukleos.sensors.Status

class SensorStuffPresenter(override val view: SensorControlInterface.View, private val mBluetoothConnector: BluetoothConnector,
                           private val mPeripheryManager: PeripheryManager) : SensorControlInterface.Presenter(view) {

    private var mSensorStatusSubscription: Disposable? = null
    private var mSensorControlSubscription: Disposable? = null

    override fun create() {}

    override fun start() {
        with(view) {
            val selectedSensor = mPeripheryManager.getSelectedSensor()
            if (selectedSensor == null) {
                disableConnectButton()
                return
            }

            showSensorStuffInformation(selectedSensor.name, selectedSensor.address)
            enableConnectButton()

            selectedSensor.apply {
                mSensorControlSubscription = this.controlObservable()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            if (it == ControlStatus.STREAMING) {
                                showScan()
                            } else {
                                showNotScan()
                            }
                        }
                mSensorStatusSubscription =
                        this.statusObservable()
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe {
                                    when (it) {
                                        Status.READY -> enableControlPanel()
                                        Status.CONNECTED -> {
                                            hideConnectionLoader()
                                            showConnected()
                                        }
                                        Status.CONNECTING -> {
                                            showConnectionLoader()
                                            showConnecting()
                                        }
                                        else -> {
                                            hideConnectionLoader()
                                            showDisconnected()
                                            disableControlPanel()
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
        mPeripheryManager.getSelectedSensor()?.apply {
            if (!isConnected()) {
                connect(mBluetoothConnector.context)
            } else {
                disconnect()
            }
        }
    }

    override fun onProgressSelected(progress: Int) {
        val sensor = mPeripheryManager.getSelectedSensor() ?: return
        val availableFrequences = sensor.getAvailableFrequencies()
        val selectedFrequency = if (progress >= 0 && progress < availableFrequences.size)
            availableFrequences[progress]
        else
            availableFrequences.last()
        sensor.setFrequency(selectedFrequency)
        view.showScanFrequency(selectedFrequency)
    }

    override fun onStartButtonClicked() {
        mPeripheryManager.getSelectedSensor()?.apply {
            if (!isStreaming()) startStreaming() else stopStreaming()
        }
    }

    override fun onVibrationClicked(vibrationDuration: Int) {
        mPeripheryManager.getSelectedSensor()?.apply {
            vibration(vibrationDuration)
        }
    }
}