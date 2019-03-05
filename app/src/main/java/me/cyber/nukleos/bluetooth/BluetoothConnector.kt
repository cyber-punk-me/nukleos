package me.cyber.nukleos.bluetooth

import  android.app.Activity
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.Context
import android.os.ParcelUuid
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.FlowableEmitter
import me.cyber.nukleus.R
import java.lang.IllegalStateException
import java.util.*
import java.util.concurrent.TimeUnit

class BluetoothConnector(val context: Context) {

    private var mBluetoothScanCallback: BluetoothScanCallback? = null
    // scan.
    fun startBluetoothScan(serviceUUID: UUID?) = Flowable.create<BluetoothDevice>({
        mBluetoothScanCallback = BluetoothScanCallback(it)
        val bluetoothScanner = getBluetoothScanner()
        if (bluetoothScanner == null) {
            it.tryOnError(IllegalStateException(context.getString(R.string.bt_adapter_problem)))
            return@create
        }
        if (serviceUUID == null) {
            bluetoothScanner.startScan(mBluetoothScanCallback)
        } else {
            bluetoothScanner.startScan(
                    arrayListOf(ScanFilter.Builder().setServiceUuid(ParcelUuid(serviceUUID)).build()),
                    ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build(),
                    mBluetoothScanCallback)
        }
    }, BackpressureStrategy.BUFFER).apply {
        doOnCancel { getBluetoothScanner()?.stopScan(mBluetoothScanCallback) }
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

    private fun getBluetoothScanner(): BluetoothLeScanner? =
            (context.getSystemService(Activity.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter?.bluetoothLeScanner


    inner class BluetoothScanCallback(private val emitter: FlowableEmitter<BluetoothDevice>) : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            result?.let {
                it.device.apply { emitter.onNext(this) }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            emitter.tryOnError(RuntimeException())
        }
    }
}
