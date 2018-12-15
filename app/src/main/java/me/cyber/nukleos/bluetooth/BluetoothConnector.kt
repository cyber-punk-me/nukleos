package me.cyber.nukleos.bluetooth

import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.ParcelUuid
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.FlowableEmitter
import java.util.*
import java.util.concurrent.TimeUnit

class BluetoothConnector(val context: Context) {

    private val mBTLowEnergyScanner = (context.getSystemService(Activity.BLUETOOTH_SERVICE) as BluetoothManager).adapter.bluetoothLeScanner

    private class BTCallbackHolder(var mBluetoothScanCallback: BluetoothScanCallback? = null)

    // scan.
    fun startBluetoothScan(serviceUUID: UUID?): Flowable<BluetoothDevice> {
        val btCallHolder = BTCallbackHolder()
        return Flowable.create<BluetoothDevice>({
            btCallHolder.mBluetoothScanCallback = BluetoothScanCallback(it)
            if (serviceUUID == null) {
                mBTLowEnergyScanner.startScan(btCallHolder.mBluetoothScanCallback)
            } else {
                val settings = ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .build()
                val filters = ArrayList<ScanFilter>()
                filters.add(ScanFilter.Builder().setServiceUuid(ParcelUuid(serviceUUID)).build())
                mBTLowEnergyScanner.startScan(filters, settings, btCallHolder.mBluetoothScanCallback)
            }
        }, BackpressureStrategy.BUFFER).apply {
            doOnCancel { mBTLowEnergyScanner.stopScan(btCallHolder.mBluetoothScanCallback) }
        }
    }

    // scan with timeout
    fun startBluetoothScan(interval: Long, timeUnit: TimeUnit, serviceUUID: UUID? = null) = startBluetoothScan(serviceUUID).takeUntil(Flowable.timer(interval, timeUnit))

/*    fun getMyo(bluetoothDevice: BluetoothDevice) = Myo(bluetoothDevice)

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
        }*/


    inner class BluetoothScanCallback(private val emitter: FlowableEmitter<BluetoothDevice>) : ScanCallback() {
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
