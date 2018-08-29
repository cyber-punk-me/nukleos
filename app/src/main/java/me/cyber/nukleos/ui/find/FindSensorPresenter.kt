package me.cyber.nukleos.ui.find

import android.bluetooth.BluetoothDevice
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import me.cyber.nukleos.dagger.SensorStuffManager
import me.cyber.nukleos.myosensor.MyoConnector
import me.cyber.nukleos.ui.model.SensorStuff
import java.util.concurrent.TimeUnit

class FindSensorPresenter(override val view: FindSensorInterface.View, private val mMyoConnector: MyoConnector, private val mSensorStuffManager: SensorStuffManager
) : FindSensorInterface.Presenter(view) {

    internal lateinit var mFindFlowable: Flowable<BluetoothDevice>

    private var mFindSubscription: Disposable? = null

    override fun create() {
        mFindFlowable = mMyoConnector.startMyoScan(10, TimeUnit.SECONDS)
    }

    override fun start() {
        with(view) {
            clearSensorList()
            if (mSensorStuffManager.findedSensorList.isEmpty()) {
                showPreparingText()
            } else {
                populateSensorList(mSensorStuffManager
                        .findedSensorList
                        .map { it -> SensorStuff(it.name, it.address) })
            }
        }
    }

    override fun destroy() {
        mFindSubscription?.dispose()
        view.hideFindLoader()
    }

    override fun onSensorSelected(index: Int) {
        mSensorStuffManager.selectedIndex = index
        view.goToSensorControl()
    }

    override fun onFindButtonClicked() {
        with(view) {
            if (mFindSubscription?.isDisposed == false) {
                mFindSubscription?.dispose()
                hideFindLoader()
                if (mSensorStuffManager.findedSensorList.isEmpty()) {
                    showEmptyListText()
                }
            } else {
                hideEmptyListText()
                showFindLoader()
                mFindSubscription = mFindFlowable
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            if (it !in mSensorStuffManager.findedSensorList) {
                                addSensorToList(SensorStuff(it.name, it.address))
                                mSensorStuffManager.findedSensorList.add(it)
                            }
                        }, {
                            hideFindLoader()
                            showFindError()
                            if (mSensorStuffManager.findedSensorList.isEmpty()) {
                                showEmptyListText()
                            }
                        }, {
                            hideFindLoader()
                            showFindSuccess()
                            if (mSensorStuffManager.findedSensorList.isEmpty()) {
                                showEmptyListText()
                            }
                        })
            }
        }
    }

}