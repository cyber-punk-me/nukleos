package me.cyber.nukleos.ui.charts

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import me.cyber.nukleos.dagger.SensorStuffManager

class ChartsPresenter(override val view: ChartInterface.View, private val mSensorStuffManager: SensorStuffManager) : ChartInterface.Presenter(view) {

    private var mChartsDataSubscription: Disposable? = null

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

    override fun destroy() {
        view.startCharts(false)
        mChartsDataSubscription?.dispose()
    }
}