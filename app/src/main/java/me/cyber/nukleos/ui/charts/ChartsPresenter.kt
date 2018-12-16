package me.cyber.nukleos.ui.charts

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import me.cyber.nukleos.App
import me.cyber.nukleos.dagger.BluetoothStuffManager
import me.cyber.nukleos.ui.charts.ChartsFragment.Companion.LEARNING_TIME
import me.cyber.nukleos.ui.charts.ChartsFragment.Companion.TIMER_COUNT
import java.util.*
import java.util.concurrent.TimeUnit

class ChartsPresenter(override val view: ChartInterface.View, private val mBluetoothStuffManager: BluetoothStuffManager) : ChartInterface.Presenter(view) {

    private val mLearningSessId = UUID.fromString("885d0665-ca5d-46ed-b6dc-ea2c2610a67f")
            //UUID.randomUUID()
    private val mScriptId = UUID.fromString("7de76908-d4d9-4ce9-98de-118a4fb3b8f8")

    private var mDataSubscription: Disposable? = null
    private var mChartsDataSubscription: Disposable? = null
    private var mPostDataSubscription: Disposable? = null
    private var mTrainModelSubscription: Disposable? = null

    override fun create() {}

    override fun start() {
        with(view) {
            mBluetoothStuffManager.myo?.apply {
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
    }

    override fun onCollectPressed() {
        val dataBuffer: ArrayList<FloatArray> = arrayListOf()
        with(view) {
            if (mBluetoothStuffManager.myo == null) {
                showNoStreamingMessage()
                return
            }
            mBluetoothStuffManager.myo?.apply {
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
                                    sendData(convertData(dataBuffer, view.getDataType(), 64, 64), mLearningSessId)
                                }
                                .subscribe {
                                    dataBuffer.add(it)
                                }

                    } else {
                        mDataSubscription?.dispose()
                    }
                } else {
                    showNoStreamingMessage()
                }
            }
        }
    }

    private fun convertData(data: List<FloatArray>, dataType: Int, window: Int = 64, slide: Int = 64) = StringBuffer().apply {
        val floats = data.flatMap { d -> d.asList() }
        var start = 0
        var end = window
        while (end <= floats.size) {
            for (i in start until end) {
                append("${floats[i]},")
            }
            append("$dataType\n")
            start += slide
            end += slide
        }
    }.toString()

    private fun sendData(data: String, learningSessId: UUID) {
        mPostDataSubscription = App.applicationComponent.getApiHelper().api.postData(learningSessId, data, "csv")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    view.goToState(ChartInterface.State.IDLE)
                    view.notifyDataSent()
                }
                        , {
                    view.goToState(ChartInterface.State.IDLE)
                    view.notifyDataFailed()
                })
    }

    override fun onTrainPressed() {
        trainModel(mLearningSessId, mScriptId)
    }

    private fun trainModel(dataId: UUID, scriptId: UUID) {
        mTrainModelSubscription = App.applicationComponent.getApiHelper().api.trainModel(dataId, scriptId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    view.goToState(ChartInterface.State.IDLE)
                    view.notifyTrainModelStarted()
                }
                        , {
                    view.goToState(ChartInterface.State.IDLE)
                    view.notifyTrainModelFailed()
                })
    }

    override fun destroy() {
        view.startCharts(false)
        mDataSubscription?.dispose()
        mChartsDataSubscription?.dispose()
        mPostDataSubscription?.dispose()
    }

    fun onCountdownFinished() {
        view.goToState(ChartInterface.State.RECORDING)
    }
}