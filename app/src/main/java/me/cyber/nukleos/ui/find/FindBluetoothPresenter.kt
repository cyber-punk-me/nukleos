package me.cyber.nukleos.ui.find

import android.bluetooth.BluetoothDevice
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import me.cyber.nukleos.dagger.BluetoothStuffManager
import me.cyber.nukleos.bluetooth.BluetoothConnector
import me.cyber.nukleos.ui.model.SensorStuff
import java.util.concurrent.TimeUnit
//bt general management
class FindBluetoothPresenter(override val view: FindSensorInterface.View, private val mBluetoothConnector: BluetoothConnector, private val mBluetoothStuffManager: BluetoothStuffManager
) : FindSensorInterface.Presenter(view) {

    internal lateinit var mFindFlowable: Flowable<BluetoothDevice>

    private var mFindSubscription: Disposable? = null

    override fun create() {
        mFindFlowable = mBluetoothConnector.startBluetoothScan(10, TimeUnit.SECONDS)
    }

    override fun start() {
        with(view) {
            clearSensorList()
            if (mBluetoothStuffManager.foundBTDevicesList.isEmpty()) {
                showPreparingText()
            } else {
                populateSensorList(mBluetoothStuffManager
                        .foundBTDevicesList
                        .map { it -> SensorStuff(it.name, it.address) })
            }
        }
    }

    override fun destroy() {
        mFindSubscription?.dispose()
        view.hideFindLoader()
    }

    override fun onSensorSelected(index: Int) {
        mBluetoothStuffManager.selectedIndex = index
        view.goToSensorControl()
    }

    override fun onFindButtonClicked() {
        with(view) {
            if (mFindSubscription?.isDisposed == false) {
                mFindSubscription?.dispose()
                hideFindLoader()
                if (mBluetoothStuffManager.foundBTDevicesList.isEmpty()) {
                    showEmptyListText()
                }
            } else {
                hideEmptyListText()
                showFindLoader()
                mFindSubscription = mFindFlowable
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            if (it !in mBluetoothStuffManager.foundBTDevicesList) {
                                addSensorToList(SensorStuff(it.name, it.address))
                                mBluetoothStuffManager.foundBTDevicesList.add(it)
                            }
                        }, {
                            hideFindLoader()
                            showFindError()
                            if (mBluetoothStuffManager.foundBTDevicesList.isEmpty()) {
                                showEmptyListText()
                            }
                        }, {
                            hideFindLoader()
                            showFindSuccess()
                            if (mBluetoothStuffManager.foundBTDevicesList.isEmpty()) {
                                showEmptyListText()
                            }
                        })
            }
        }
    }

}