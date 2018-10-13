package me.cyber.nukleos.ui.export

import android.util.Log
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import me.cyber.nukleos.dagger.SensorStuffManager
import me.cyber.nukleos.ui.export.NNLearningFragment.Companion.LEARNING_TIME
import me.cyber.nukleos.ui.export.NNLearningFragment.Companion.TIMER_COUNT
import java.util.*
import java.util.concurrent.TimeUnit

class NNLearningPresenter(override val view: NNLearningInterface.View, private val mSensorStuffManager: SensorStuffManager) : NNLearningInterface.Presenter(view) {

    private val mDataBuffer: ArrayList<FloatArray> = arrayListOf()
    private var mDataType: Int = -1

    private var mDataSubscription: Disposable? = null

    override fun create() {}

    override fun start() {
        with(view) {
            mSensorStuffManager.myo?.apply {
                if (this.isStreaming()) {
                    enableStartCollectingButton()
                } else {
                    disableStartCollectingButton()
                }
            }
        }
    }

    override fun onBufferDataPressed(dataType: Int) {
        mDataType = dataType
        with(view) {
            showCoundtown()
            mSensorStuffManager.myo?.apply {
                if (this.isStreaming()) {
                    if (mDataSubscription == null || mDataSubscription?.isDisposed == true) {
                        mDataSubscription =
                                this.dataFlowable()
                                        .skip(TIMER_COUNT, TimeUnit.SECONDS)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .doOnSubscribe {
                                            showCollectionStarted()
                                            disableResetButton()
                                        }
                                        .take(TIMER_COUNT + LEARNING_TIME, TimeUnit.SECONDS)
                                        .doOnComplete {
                                            //todo отправить  закончено накопление говна. предложить отправить
                                            Log.e("-----", "===============VSE=======$dataType=========")
                                        }
                                        .subscribe {
                                            //todo отправить накопление говна
                                            mDataBuffer.add(it)
                                        }
                    } else {
                        mDataSubscription?.dispose()
                    }
                } else {
                    showNotStreamingErrorMessage()
                }
            }
        }
    }

    override fun onResetPressed() {
        with(view) {
            mDataBuffer.clear()
            mDataSubscription?.dispose()
            hideSaveArea()
            disableResetButton()
        }
    }

    override fun onSavePressed() {
        view.saveCsvFile(createStringFromData(mDataBuffer))
    }

    override fun onSendPressed() {
        view.sendData(createStringFromData(mDataBuffer))
    }

    private fun createStringFromData(buffer: ArrayList<FloatArray>) = StringBuilder().apply {
        buffer.forEach {
            it.forEach {
                append(it)
                append(",")
            }
            append(" $mDataType;")
            append("\n")
        }
    }.toString()

    override fun destroy() {
        mDataSubscription?.dispose()
        view.showCollectionStopped()
    }
}