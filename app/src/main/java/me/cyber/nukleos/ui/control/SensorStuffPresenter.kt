package me.cyber.nukleos.ui.control

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import me.cyber.nukleos.bluetooth.BluetoothConnector
import me.cyber.nukleos.dagger.BluetoothStuffManager
import me.cyber.nukleos.myosensor.*

class SensorStuffPresenter(override val view: SensorControlInterface.View, private val mBluetoothConnector: BluetoothConnector,
                           private val mBluetoothStuffManager: BluetoothStuffManager) : SensorControlInterface.Presenter(view) {

    private var mSensorStatusSubscription: Disposable? = null
    private var mSensorControlSubscription: Disposable? = null

    override fun create() {}

    override fun start() {
        with(view) {
            if (mBluetoothStuffManager.selectedIndex == -1) {
                disableConnectButton()
                return
            }

            val currentSensorStuff = mBluetoothStuffManager.foundBTDevicesList[mBluetoothStuffManager.selectedIndex]
            showSensorStuffInformation(currentSensorStuff.name, currentSensorStuff.address)
            enableConnectButton()
            if (mBluetoothStuffManager.myo == null) {
                mBluetoothStuffManager.myo = Myo(currentSensorStuff)
            }

            mBluetoothStuffManager.myo?.apply {
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
        mBluetoothStuffManager.myo?.apply {
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
        mBluetoothStuffManager.myo?.apply {
            frequency = selectedFrequency
        }
    }

    override fun onScanButtonClicked() {
        mBluetoothStuffManager.myo?.apply {
            if (!isStreaming()) {
                sendCommand(CommandList.emgFilteredOnly())
            } else {
                sendCommand(CommandList.stopStreaming())
            }
        }
    }

    override fun onVibrationClicked(vibrationDuration: Int) {
        mBluetoothStuffManager.myo?.apply {
            sendCommand(when (vibrationDuration) {
                1 -> CommandList.vibration1()
                2 -> CommandList.vibration2()
                else -> CommandList.vibration3()
            })
        }
    }
}