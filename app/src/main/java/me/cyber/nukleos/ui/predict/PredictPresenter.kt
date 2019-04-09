package me.cyber.nukleos.ui.predict

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import me.cyber.nukleos.App
import me.cyber.nukleos.IMotors
import me.cyber.nukleos.api.PredictRequest
import me.cyber.nukleos.api.PredictResponse
import me.cyber.nukleos.api.Prediction
import me.cyber.nukleos.control.TryControl
import me.cyber.nukleos.dagger.PeripheryManager
import me.cyber.nukleos.utils.LimitedQueue

class PredictPresenter(override val view: PredictInterface.View, private val mPeripheryManager: PeripheryManager) : PredictInterface.Presenter(view) {

    private var mChartsDataSubscription: Disposable? = null
    private var mPostPredict: Disposable? = null
    private var predictEnabled = false
    private var predictBuffer = LimitedQueue<FloatArray>(8)
    private var iUpdate = 0
    private val updatesUntilPredict = 4
    private val control = TryControl()

    override fun create() {}

    override fun start() {
        with(view) {
            val selectedSensor = mPeripheryManager.getSelectedSensor() ?: return
            selectedSensor.apply {
                hideNoStreamingMessage()
                mChartsDataSubscription?.apply {
                    if (isDisposed) this.dispose()
                }
                mChartsDataSubscription = this.getDataFlowable()
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
                    .also { it.add(predictBuffer.flatMap { d -> d.asList() }) })

            mPostPredict = App.applicationComponent.getApiHelper().api.predict(predictRequest)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        val predClass = it.predictions[0].output

                        val tryControl = control.guess(predClass)

                        if (tryControl >= 0) {
                            view.notifyPredict(
                                    PredictResponse(listOf(Prediction(tryControl, it.predictions[0].distr))))
                            if (mPeripheryManager.motors != null) {
                                val mot = mPeripheryManager.motors!!
                                when (tryControl) {
                                    0 -> mot.stopMotors()
                                    1 -> mot.spinMotor(1, IMotors.FORWARD, 75)
                                    2 -> mot.spinMotor(1, IMotors.BACKWARD, 75)
                                    3 -> mot.spinMotor(2, IMotors.FORWARD, 75)
                                    4 -> mot.spinMotor(2, IMotors.BACKWARD, 75)
                                }
                            }

                        }
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