package me.cyber.nukleos.ui.control

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import me.cyber.nukleos.bluetooth.BluetoothConnector
import me.cyber.nukleos.dagger.PeripheryManager
import me.cyber.nukleos.motors.MotorsBlueTooth
import me.cyber.nukleos.sensors.Status

class PeripheryStuffPresenter(override val view: PeripheryControlInterface.View, private val mBluetoothConnector: BluetoothConnector,
                              private val mPeripheryManager: PeripheryManager) : PeripheryControlInterface.Presenter(view) {

    private var mSensorStatusSubscription: Disposable? = null
    private var mSensorControlSubscription: Disposable? = null
    private var mMotorsStatusSubscription: Disposable? = null


    override fun create() {
        mPeripheryManager.motors = MotorsBlueTooth(mPeripheryManager, mBluetoothConnector)
        mPeripheryManager.connectMotors()
    }

    override fun start() {
        with(view) {
            mMotorsStatusSubscription = mPeripheryManager.motorsObservable
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe{
                        when (it.isConnected()) {
                            true -> showMotorsConnected()
                            false -> showMotorsDisonnected()
                        }
                    }

            val selectedSensor = mPeripheryManager.getLastSelectedSensor()
            if (selectedSensor == null) {
                disableSensorConnectButton()
            } else {
                showSensorStuffInformation(selectedSensor.name, selectedSensor.address)
                enableSensorConnectButton()
                selectedSensor.apply {
                    mSensorStatusSubscription =
                            this.statusObservable()
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe {
                                        when (it) {
                                            Status.CONNECTING -> {
                                                showConnectionLoader()
                                                showSensorConnecting()
                                                showSensorNotScan()
                                            }
                                            Status.STREAMING -> {
                                                hideConnectionLoader()
                                                showSensorConnected()
                                                showSensorScan()
                                            }
                                            else -> {
                                                hideConnectionLoader()
                                                showSensorDisconnected()
                                                disableSensorControlPanel()
                                                showSensorNotScan()
                                            }
                                        }
                                    }
                }
            }
        }
    }

    override fun destroy() {
        mSensorStatusSubscription?.dispose()
        mSensorControlSubscription?.dispose()
        mMotorsStatusSubscription?.dispose()
    }

    override fun onConnectSensorClicked() {
        mPeripheryManager.getLastSelectedSensor()?.apply {
            if (!isConnected()) {
                connect()
            } else {
                disconnect()
            }
        }
    }

    override fun onConnectMotorsClicked() {
            if (!mPeripheryManager.motors.isConnected()) {
                mPeripheryManager.connectMotors()
            } else {
                mPeripheryManager.disconnectMotors()
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
        view.showSensorScanFrequency(selectedFrequency)
    }

    override fun onVibrationClicked(vibrationDuration: Int) {
        mPeripheryManager.getLastSelectedSensor()?.apply {
            feedback("$vibrationDuration")
        }
    }
}