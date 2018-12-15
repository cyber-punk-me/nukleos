package me.cyber.nukleos.ui.control

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import me.cyber.nukleos.bluetooth.BluetoothConnector
import me.cyber.nukleos.dagger.SensorStuffManager
import me.cyber.nukleos.myosensor.*

class SensorStuffPresenter(override val view: SensorControlInterface.View, private val mBluetoothConnector: BluetoothConnector,
                           private val mSensorStuffManager: SensorStuffManager) : SensorControlInterface.Presenter(view) {

    private var mSensorStatusSubscription: Disposable? = null
    private var mSensorControlSubscription: Disposable? = null

    override fun create() {}

    override fun start() {
        with(view) {
            if (mSensorStuffManager.selectedIndex == -1) {
                disableConnectButton()
                return
            }

            val currentSensorStuff = mSensorStuffManager.findedSensorList[mSensorStuffManager.selectedIndex]
            showSensorStuffInformation(currentSensorStuff.name, currentSensorStuff.address)
            enableConnectButton()
            if (mSensorStuffManager.myo == null) {
                mSensorStuffManager.myo = mBluetoothConnector.getMyo(currentSensorStuff)
            }

            mSensorStuffManager.myo?.apply {
                mSensorControlSubscription = this.controlObservable()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            if (it == MyoControlStatus.STREAMING) {
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
                                        MyoStatus.READY -> enableControlPanel()
                                        MyoStatus.CONNECTED -> {
                                            hideConnectionLoader()
                                            showConnected()
                                        }
                                        MyoStatus.CONNECTING -> {
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
        mSensorStuffManager.myo?.apply {
            if (!isConnected()) {
                connect(mBluetoothConnector.context)
            } else {
                disconnect()
            }
        }
    }

    override fun onProgressSelected(progress: Int) {
        val selectedFrequency = when (progress) {
            0 -> 1
            1 -> 10
            2 -> 50
            3 -> 100
            else -> MYO_MAX_FREQUENCY
        }
        view.showScanFrequency(selectedFrequency)
        mSensorStuffManager.myo?.apply {
            frequency = selectedFrequency
        }
    }

    override fun onScanButtonClicked() {
        mSensorStuffManager.myo?.apply {
            if (!isStreaming()) {
                sendCommand(CommandList.emgFilteredOnly())
            } else {
                sendCommand(CommandList.stopStreaming())
            }
        }
    }

    override fun onVibrationClicked(vibrationDuration: Int) {
        mSensorStuffManager.myo?.apply {
            sendCommand(when (vibrationDuration) {
                1 -> CommandList.vibration1()
                2 -> CommandList.vibration2()
                else -> CommandList.vibration3()
            })
        }
    }
}