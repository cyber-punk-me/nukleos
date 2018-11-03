package me.cyber.nukleos.ui.predict

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import me.cyber.nukleos.App
import me.cyber.nukleos.api.PredictRequest
import me.cyber.nukleos.dagger.SensorStuffManager
import me.cyber.nukleos.utils.LimitedQueue

class PredictPresenter(override val view: PredictInterface.View, private val mSensorStuffManager: SensorStuffManager) : PredictInterface.Presenter(view) {

    private var mChartsDataSubscription: Disposable? = null
    private var mPostPredict: Disposable? = null
    private var predictEnabled = false
    private var predictBuffer = LimitedQueue<FloatArray>(8)
    private var iUpdate = 0
    private val updatesUntilPredict = 512

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
                            .subscribe {
                                showData(it)
                                predictBuffer.add(it)
                                if (predictEnabled) {
                                    predict()
                                }
                            }
                } else {
                    showNoStreamingMessage()
                }
            }
        }
    }

    private fun predict() {
        iUpdate++
        if (iUpdate >= updatesUntilPredict) {
            iUpdate = 0
            doPredict()
        }
    }

    private fun doPredict() {
        if (predictBuffer.size == 8) {
            val predictRequest = PredictRequest(ArrayList<List<Float>>()
                    .also{ it.add(predictBuffer.flatMap { d -> d.asList() }) })

            mPostPredict = App.applicationComponent.getApiHelper().api.predict(predictRequest)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        view.notifyPredict(it)
                    }
                            , {
                        view.notifyPredictError(it)
                    })
        }
    }

    override fun onPredictSwitched(on: Boolean) {
        predictEnabled = on
        predictBuffer = LimitedQueue(8)
    }

    override fun destroy() {
        view.startCharts(false)
        mChartsDataSubscription?.dispose()
        mPostPredict?.dispose()
    }

}