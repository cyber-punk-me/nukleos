package me.cyber.nukleos.extensions

import android.util.Log
import com.thalmic.myo.GattCallback
import com.thalmic.myo.Hub
import com.thalmic.myo.Myo
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import java.util.*

/**
 * Extensions for hub
 */

fun Hub.setRXListener() = Flowable.create<GattValues>({ e ->
    this.addGattValueListener(object : GattCallback.ValueListener {
        override fun onCharacteristicChanged(myo: Myo, uuid: UUID, bArr: ByteArray) {
            e.onNext(GattValues(myo, uuid, bArr))
        }
    })
    e.setCancellable { Log.e("--ONERROR--", "---focus on GattValueListener. It makes something wrong!---") }
}, BackpressureStrategy.BUFFER)

data class GattValues(val myo: Myo, val uuid: UUID, val byteArray: ByteArray)

fun Disposable?.safeDispose() = this?.let {
    if (!it.isDisposed) {
        dispose()
    }
} ?: Unit


fun <T> Observable<T>.shortSubscription(onNext: (T) -> Unit,
                                        onError: (Throwable) -> Unit = { it.printStackTrace() },
                                        onComplete: () -> Unit = {}) = subscribe(onNext, onError, onComplete)
