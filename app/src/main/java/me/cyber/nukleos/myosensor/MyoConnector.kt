package me.cyber.nukleos.myosensor

import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.FlowableEmitter
import io.reactivex.Single
import java.util.concurrent.TimeUnit

class MyoConnector(val context: Context) {

    private val mBTLowEnergyScanner = (context.getSystemService(Activity.BLUETOOTH_SERVICE) as BluetoothManager).adapter.bluetoothLeScanner

    private var mMyoConnectorScanCallback: MyoConnectorScanCallback? = null

    // scan.
    fun startMyoScan() = Flowable.create<BluetoothDevice>({
        mMyoConnectorScanCallback = MyoConnectorScanCallback(it)
        mBTLowEnergyScanner.startScan(mMyoConnectorScanCallback)
    }, BackpressureStrategy.BUFFER).apply {
        doOnCancel { mBTLowEnergyScanner.stopScan(mMyoConnectorScanCallback) }
    }


    // scan with timeout
    fun startMyoScan(interval: Long, timeUnit: TimeUnit) = startMyoScan().takeUntil(Flowable.timer(interval, timeUnit))

    fun getMyo(bluetoothDevice: BluetoothDevice) =Myo(bluetoothDevice)

    fun getMyo(myoAddress: String) = Single.create<Myo> {
            mBTLowEnergyScanner.startScan(listOf(ScanFilter.Builder()
                    .setDeviceAddress(myoAddress)
                    .build()), ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                    .build(), object : ScanCallback() {
                override fun onScanResult(callbackType: Int, result: ScanResult?) {
                    super.onScanResult(callbackType, result)
                    result?.device?.apply {
                        it.onSuccess(Myo(this))
                    }
                }
                override fun onScanFailed(errorCode: Int) {
                    super.onScanFailed(errorCode)
                    it.onError(RuntimeException())
                }
            })
        }


    inner class MyoConnectorScanCallback(private val emitter: FlowableEmitter<BluetoothDevice>) : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            result?.let {
                it.device.apply { emitter.onNext(this) }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            emitter.onError(RuntimeException())
        }
    }
}