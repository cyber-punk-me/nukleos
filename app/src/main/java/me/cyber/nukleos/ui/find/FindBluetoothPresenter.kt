package me.cyber.nukleos.ui.find

import android.bluetooth.BluetoothDevice
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import me.cyber.nukleos.IMotors
import me.cyber.nukleos.bluetooth.BluetoothConnector
import me.cyber.nukleos.dagger.PeripheryManager
import me.cyber.nukleos.motors.MotorsBlueTooth
import me.cyber.nukleos.sensors.myosensor.Myo
import java.util.concurrent.TimeUnit

//bt general management
class FindBluetoothPresenter(
        override val view: FindSensorInterface.View,
        private val mBluetoothConnector: BluetoothConnector,
        private val mPeripheryManager: PeripheryManager
) : FindSensorInterface.Presenter(view) {

    private var mFindFlowable: Flowable<BluetoothDevice>? = null
    private var mFindMotorsFlowable: Flowable<BluetoothDevice>? = null

    private var mFindSubscription: Disposable? = null
    private var mFindMotorsSubscription: Disposable? = null

    private var mSensorsUpdateSubscription: Disposable? = null

    override fun create() {
        mFindFlowable = mBluetoothConnector.startBluetoothScan(10, TimeUnit.SECONDS, Myo.BLUETOOTH_UUID)
        mFindMotorsFlowable = mBluetoothConnector.startBluetoothScan(10, TimeUnit.SECONDS, IMotors.SERVICE_UUID)
                .apply {
                    mFindMotorsSubscription = subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({
                                if (mPeripheryManager.motors !is MotorsBlueTooth) {
                                    synchronized(mPeripheryManager) {
                                        if (mPeripheryManager.motors !is MotorsBlueTooth) {
                                            val motors = MotorsBlueTooth(it, mPeripheryManager)
                                            mPeripheryManager.motors = motors
                                            motors.connect(mBluetoothConnector.context)
                                        }
                                    }
                                }
                            }, {}, {})
                }
    }

    override fun start() = with(view) {
        mSensorsUpdateSubscription =
                mPeripheryManager.
                        activeSensorsObservable.
                        observeOn(AndroidSchedulers.mainThread()).
                        subscribe {
                            populateSensors(it)
                        }
    }

    override fun destroy() {
        mFindSubscription?.dispose()
        mFindMotorsSubscription?.dispose()
        mSensorsUpdateSubscription?.dispose()
        view.hideFindLoader()
    }

    override fun onSensorSelected(index: Int) {
        val model = view.getSensorModel(index)
        val id = model?.id ?: return
        mPeripheryManager.setLastSelectedSensorById(id)
        view.goToSensorControl()
    }

    override fun onFindButtonClicked() = with(view) {
            if (mFindSubscription?.isDisposed == false) {
                mFindSubscription?.dispose()
                hideFindLoader()
                if (!mPeripheryManager.hasSensors()) {
                    showEmptyListText()
                }
            } else {
                hideEmptyListText()
                mPeripheryManager.removeIf { it is Myo }
                clearSensorList()
                showFindLoader()
                mFindSubscription = mFindFlowable
                        ?.subscribeOn(Schedulers.io())
                        ?.observeOn(AndroidSchedulers.mainThread())
                        ?.subscribe({
                            val sensor = Myo(it)
                            if (sensor !in mPeripheryManager.getSensors()) {
                                mPeripheryManager.addSensor(sensor)

                            }
                        }, {
                            hideFindLoader()
                            showFindError(it.message)
                            if (!mPeripheryManager.hasSensors()) {
                                showEmptyListText()
                            }
                        }, {
                            hideFindLoader()
                            showFindSuccess()
                            if (!mPeripheryManager.hasSensors()) {
                                showEmptyListText()
                            }
                        })
            }
        }
}