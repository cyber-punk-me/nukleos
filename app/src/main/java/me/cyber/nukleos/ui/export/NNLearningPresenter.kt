package me.cyber.nukleos.ui.export

import android.util.Log
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import me.cyber.nukleos.App
import me.cyber.nukleos.dagger.SensorStuffManager
import me.cyber.nukleos.ui.export.NNLearningFragment.Companion.LEARNING_TIME
import me.cyber.nukleos.ui.export.NNLearningFragment.Companion.TIMER_COUNT
import java.util.*
import java.util.concurrent.TimeUnit

class NNLearningPresenter(override val view: NNLearningInterface.View, private val mSensorStuffManager: SensorStuffManager) : NNLearningInterface.Presenter(view) {

    private val mDataBuffer: ArrayList<FloatArray> = arrayListOf()
    private var mDataType: Int = -1

    private var mDataSubscription: Disposable? = null
    private val mLearningSessId = UUID.randomUUID()

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
                                            sendData(convertData(mDataBuffer, dataType, 64, 8), mLearningSessId)
                                            mDataBuffer.clear()
                                        }
                                        .subscribe {
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

    private fun convertData(data: List<FloatArray>, dataType: Int, window: Int = 64, slide : Int = 64): String {
        val result = StringBuffer()
        val floats = data.flatMap { d -> d.asList() }
        var start = 0
        var end = window
        while (end <= floats.size) {
            for (i in start until end) {
                result.append("${floats[i]},")
            }
            result.append("$dataType\n")
            start += slide
            end += slide
        }
        return result.toString()
    }

    private fun sendData(data: String, learningSessId: UUID) = App.applicationComponent.getApiHelper().api.postData(learningSessId, data, "csv")
            .subscribe({ Log.e("-----", "======${it.id}") }
                    , { Log.e("=Error=", "=============${it.message}============") })

    override fun onResetPressed() {
        with(view) {
            mDataBuffer.clear()
            mDataSubscription?.dispose()
            disableResetButton()
        }
    }

    override fun onSavePressed() {
        view.saveDataFile(createStringFromData(mDataBuffer))
    }


    private fun createStringFromData(buffer: ArrayList<FloatArray>) = StringBuilder().apply {
        buffer.forEach {
            it.forEach {
                append(it)
                append(",")
            }
        }
        append("$mDataType")
    }.toString()

    override fun destroy() {
        mDataSubscription?.dispose()
        view.showCollectionStopped()
    }
}