package me.cyber.nukleos.ui.charts

import android.util.Log
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import me.cyber.nukleos.App
import me.cyber.nukleos.dagger.SensorStuffManager
import me.cyber.nukleos.ui.charts.ChartsFragment.Companion.LEARNING_TIME
import me.cyber.nukleos.ui.charts.ChartsFragment.Companion.TIMER_COUNT
import java.util.*
import java.util.concurrent.TimeUnit

class ChartsPresenter(override val view: ChartInterface.View, private val mSensorStuffManager: SensorStuffManager) : ChartInterface.Presenter(view) {

    private val mDataBuffer: ArrayList<FloatArray> = arrayListOf()

    private var mDataSubscription: Disposable? = null
    private val mLearningSessId = UUID.randomUUID()
    private var mChartsDataSubscription: Disposable? = null
    private var mRequestSubscription: Disposable? = null

    override fun create() {}

    override fun start() {
        with(view) {
            mSensorStuffManager.myo?.apply {
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
        with(view) {
            showCountdown()
            mSensorStuffManager.myo?.apply {
                if (this.isStreaming()) {
                    if (mDataSubscription == null || mDataSubscription?.isDisposed == true) {
                        mDataSubscription = this.dataFlowable()
                                .skip(TIMER_COUNT, TimeUnit.SECONDS)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .take(TIMER_COUNT + LEARNING_TIME, TimeUnit.SECONDS)
                                .subscribe {
                                    mDataBuffer.add(it)
                                    learningIsFinish()
                                    readyForSending()
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

    override fun onSavePressed() {
        sendData(convertData(mDataBuffer, view.getDataType(), 64, 8), mLearningSessId)
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
        mRequestSubscription = App.applicationComponent.getApiHelper().api.postData(learningSessId, data, "csv")
                .doOnDispose { view.newCollecting() }
                .subscribe({ Log.e("-----", "======${it.id}") }
                        , { Log.e("=Error=", "=============${it.message}============") })
    }

    override fun destroy() {
        view.startCharts(false)
        mChartsDataSubscription?.dispose()
        mRequestSubscription?.dispose()
    }
}