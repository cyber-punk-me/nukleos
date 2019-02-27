package me.cyber.nukleos.ui.find

import android.bluetooth.BluetoothDevice
import me.cyber.nukleos.IMotors
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import me.cyber.nukleos.dagger.PeripheryManager
import me.cyber.nukleos.bluetooth.BluetoothConnector
import me.cyber.nukleos.motors.MotorsBlueTooth
import me.cyber.nukleos.model.SensorStuff
import java.util.concurrent.TimeUnit

//bt general management
class FindBluetoothPresenter(override val view: FindSensorInterface.View, private val mBluetoothConnector: BluetoothConnector, private val mPeripheryManager: PeripheryManager
) : FindSensorInterface.Presenter(view) {

    private var mFindFlowable: Flowable<BluetoothDevice>? = null
    private var mFindMotorsFlowable: Flowable<BluetoothDevice>? = null

    private var mFindSubscription: Disposable? = null
    private var mFindMotorsSubscription: Disposable? = null

    override fun create() {
        mFindFlowable = mBluetoothConnector.startBluetoothScan(10, TimeUnit.SECONDS)
        mFindMotorsFlowable = mBluetoothConnector.startBluetoothScan(10, TimeUnit.SECONDS, IMotors.SERVICE_UUID)
                .apply {
                    mFindMotorsSubscription = subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({
                                if (mPeripheryManager.motors == null) {
                                    synchronized(mPeripheryManager) {
                                        if (mPeripheryManager.motors == null) {
                                            val motors = MotorsBlueTooth(it)
                                            mPeripheryManager.motors = motors
                                            motors.connect(mBluetoothConnector.context)
                                        }
                                    }
                                }
                            }, {}, {})
                }
    }

    override fun start() = with(view) {
        clearSensorList()
        if (mPeripheryManager.foundBTDevicesList.isEmpty()) {
            showPreparingText()
        } else {
            populateSensorList(mPeripheryManager.foundBTDevicesList.map { it -> SensorStuff(it.name, it.address) })
        }

    }

    override fun destroy() {
        mFindSubscription?.dispose()
        mFindMotorsSubscription?.dispose()
        view.hideFindLoader()
    }

    override fun onSensorSelected(index: Int) {
        mPeripheryManager.selectedIndex = index
        view.goToSensorControl()
    }

    override fun onFindButtonClicked() = with(view) {
            if (mFindSubscription?.isDisposed == false) {
                mFindSubscription?.dispose()
                hideFindLoader()
                if (mPeripheryManager.foundBTDevicesList.isEmpty()) {
                    showEmptyListText()
                }
            } else {
                hideEmptyListText()
                showFindLoader()
                mFindSubscription = mFindFlowable
                        ?.subscribeOn(Schedulers.io())
                        ?.observeOn(AndroidSchedulers.mainThread())
                        ?.subscribe({
                            if (it !in mPeripheryManager.foundBTDevicesList) {
                                addSensorToList(SensorStuff(it.name, it.address))
                                mPeripheryManager.foundBTDevicesList.add(it)
                            }
                        }, {
                            hideFindLoader()
                            showFindError()
                            if (mPeripheryManager.foundBTDevicesList.isEmpty()) {
                                showEmptyListText()
                            }
                        }, {
                            hideFindLoader()
                            showFindSuccess()
                            if (mPeripheryManager.foundBTDevicesList.isEmpty()) {
                                showEmptyListText()
                            }
                        })
            }
        }
}