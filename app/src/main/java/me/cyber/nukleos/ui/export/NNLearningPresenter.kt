package me.cyber.nukleos.ui.export

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import me.cyber.nukleos.dagger.SensorStuffManager
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class NNLearningPresenter(override val view: NNLearningInterface.View, private val mSensorStuffManager: SensorStuffManager) : NNLearningInterface.Presenter(view) {

    private val mValuesCounter: AtomicInteger = AtomicInteger()
    private val mDataBuffer: ArrayList<FloatArray> = arrayListOf()

    private var mDataSubscription: Disposable? = null

    override fun create() {}

    override fun start() {
        with(view) {
            showCollectedData(mValuesCounter.get())
            mSensorStuffManager.myo?.apply {
                if (this.isStreaming()) {
                    enableStartCollectingButton()
                } else {
                    disableStartCollectingButton()
                }
            }
        }
    }

    override fun onBufferDataPressed() {
        with(view) {
            mSensorStuffManager.myo?.apply {
                if (this.isStreaming()) {
                    if (mDataSubscription == null || mDataSubscription?.isDisposed == true) {
                        mDataSubscription = this.dataFlowable()
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .doOnSubscribe {
                                    showCollectionStarted()
                                    disableResetButton()
                                }
                                .subscribe {
                                    mDataBuffer.add(it)
                                    showCollectedData(mValuesCounter.incrementAndGet())
                                }
                    } else {
                        mDataSubscription?.dispose()
                        enableResetButton()
                        showSaveArea()
                        showCollectionStopped()
                    }
                } else {
                    showNotStreamingErrorMessage()
                }
            }
        }
    }

    override fun onResetPressed() {
        with(view) {
            mValuesCounter.set(0)
            mDataBuffer.clear()
            showCollectedData(0)
            mDataSubscription?.dispose()
            hideSaveArea()
            disableResetButton()
        }
    }

    override fun onSavePressed() {
        view.saveDataFile(createStringFromData(mDataBuffer))
    }

    override fun onSendPressed() {
        view.sendData(createStringFromData(mDataBuffer))
    }

    private fun createStringFromData(buffer: ArrayList<FloatArray>) = StringBuilder().apply {
        buffer.forEach {
            it.forEach {
                append(it)
                append(";")
            }
            append("\n")
        }
    }.toString()

    override fun destroy() {
        mDataSubscription?.dispose()
        view.showCollectionStopped()
    }
}