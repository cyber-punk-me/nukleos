package me.cyber.nukleos.ui.charts

import android.util.Log
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import me.cyber.nukleos.App
import me.cyber.nukleos.dagger.PeripheryManager
import me.cyber.nukleos.ui.charts.ChartsFragment.Companion.LEARNING_TIME
import me.cyber.nukleos.ui.charts.ChartsFragment.Companion.TIMER_COUNT
import java.util.*
import java.util.concurrent.TimeUnit

class ChartsPresenter(override val view: ChartInterface.View, private val mPeripheryManager: PeripheryManager) : ChartInterface.Presenter(view) {

    private val TAG = "ChartsPresenter"
    private val millsBetweenReads = 5
    private val numSensors = 8
    private val mApi by lazy { App.applicationComponent.getApiHelper().api }
    private val mLearningSessId = UUID.fromString("885d0665-ca5d-46ed-b6dc-ea2c2610a67f")
    private val mScriptId = UUID.fromString("7de76908-d4d9-4ce9-98de-118a4fb3b8f8")
    private var mServerTimeMinusLocal: Long = 0

    private var mDataSubscription: Disposable? = null
    private var mChartsDataSubscription: Disposable? = null
    private var mPostDataSubscription: Disposable? = null
    private var mTrainModelSubscription: Disposable? = null
    private var mServerTimeSubscription: Disposable? = null
    private var mMarkTime = false

    private fun convertData(data: List<FloatArray>, dataType: Int, window: Int = 64, slide: Int = 64) =
            StringBuffer().apply {
                val recordEndTime = System.currentTimeMillis() + mServerTimeMinusLocal
                val recordStartTime = recordEndTime - millsBetweenReads * data.size
                val floats = data.flatMap { d -> d.asList() }
                var start = 0
                var end = window
                while (end <= floats.size) {
                    for (i in start until end) {
                        append("${floats[i]},")
                    }
                    if (!mMarkTime) {
                        append("$dataType\n")
                    } else {
                        val windowTime = recordStartTime + start * millsBetweenReads / numSensors
                        append("$windowTime\n")
                    }
                    start += slide
                    end += slide
                }
            }.toString()

    private fun sendData(data: String, learningSessId: UUID) = with(view) {
        mPostDataSubscription = mApi.postData(learningSessId, data, "csv")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    goToState(ChartInterface.State.IDLE)
                    notifyDataSent()
                }, {
                    goToState(ChartInterface.State.IDLE)
                    notifyDataFailed()
                })
    }

    private fun trainModel(dataId: UUID, scriptId: UUID) {
        mTrainModelSubscription = mApi.trainModel(dataId, scriptId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    view.goToState(ChartInterface.State.IDLE)
                    view.notifyTrainModelStarted()
                }, {
                    view.goToState(ChartInterface.State.IDLE)
                    view.notifyTrainModelFailed()
                })
    }

    private fun getServerTimeDiff() {
        mServerTimeSubscription = mApi.getServerTime()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    mServerTimeMinusLocal = it - System.currentTimeMillis()
                    Log.i(TAG, "mServerTimeMinusLocal: $mServerTimeMinusLocal")
                }, {
                    view.notifyDataFailed()
                })
    }

    fun onCountdownFinished() = view.goToState(ChartInterface.State.RECORDING)

    override fun create() {}

    override fun start() {
        with(view) {
            mPeripheryManager.myo?.apply {
                if (this.isStreaming()) {
                    hideNoStreamingMessage()
                    mChartsDataSubscription?.apply {
                        if (isDisposed) this.dispose()
                    }
                    mChartsDataSubscription = this.dataFlowable()
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnSubscribe { startCharts(true) }
                            .subscribe { showData(it) }
                } else {
                    showNoStreamingMessage()
                }
            }
        }
        getServerTimeDiff()
    }

    override fun onCollectPressed() {
        val dataBuffer: ArrayList<FloatArray> = arrayListOf()
        with(view) {
            if (mPeripheryManager.synapsUsbHandler == null) {
                showNoStreamingMessage()
                return
            }
            mPeripheryManager.synapsUsbHandler?.apply {
                if (this.isStreaming()) {
                    goToState(ChartInterface.State.COUNTDOWN)
                    hideNoStreamingMessage()
                    if (mDataSubscription == null || mDataSubscription?.isDisposed == true) {
                        mDataSubscription = this.dataFlowable()
                                .skip(TIMER_COUNT, TimeUnit.SECONDS)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .take(TIMER_COUNT + LEARNING_TIME, TimeUnit.SECONDS)
                                .doOnComplete {
                                    goToState(ChartInterface.State.SENDING)
                                    sendData(convertData(dataBuffer,
                                            view.getDataType(), 64, 64),
                                            mLearningSessId)
                                }
                                .subscribe { dataBuffer.add(it) }
                    } else {
                        mDataSubscription?.dispose()
                    }
                } else {
                    showNoStreamingMessage()
                }
            }
        }
    }

    override fun onTrainPressed() = trainModel(mLearningSessId, mScriptId)

    override fun destroy() {
        view.startCharts(false)
        mDataSubscription?.dispose()
        mChartsDataSubscription?.dispose()
        mPostDataSubscription?.dispose()
        mServerTimeSubscription?.dispose()
    }
}