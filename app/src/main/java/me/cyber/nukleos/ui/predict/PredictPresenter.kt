package me.cyber.nukleos.ui.predict

import android.content.Intent
import io.reactivex.disposables.Disposable
import me.cyber.nukleos.App
import me.cyber.nukleos.IMotors
import me.cyber.nukleos.api.PredictResponse
import me.cyber.nukleos.api.Prediction
import me.cyber.nukleos.control.TryControl
import me.cyber.nukleos.dagger.PeripheryManager
import me.cyber.nukleos.sensors.Sensor
import me.cyber.nukleos.sensors.SensorListener
import me.cyber.nukleos.utils.LimitedQueue

class PredictPresenter(override val view: PredictInterface.View, private val mPeripheryManager: PeripheryManager)
    : PredictInterface.Presenter(view), SensorListener {

    private val TAG = "PredictPresenter"

    private var mChartsDataSubscription: Disposable? = null
    private var mDownloadModelSubscription: Disposable? = null
    private val predictionResultReceiver = PredictionResultReceiver(
            { predictedClass, distribution ->
                onPredictionResult(predictedClass, distribution) },
            { onPredictionError(Exception(it)) })
    private var mPostPredict: Disposable? = null
    private var predictEnabled = false
    private var predictOnlineEnabled = false
    private var predictBuffer = LimitedQueue<FloatArray>(8)
    private var iUpdate = 0
    private val updatesUntilPredictOnline = 4
    private val control = TryControl()

    @Volatile private var predictionInProgress = false

    override fun create() {}

    override fun start() {
        Sensor.registerSensorListener(TAG, this)
        with(view) {
            startCharts(true)
        }
        initializePrediction()
    }

    override fun onSensorData(sensorName: String, vararg data: FloatArray) {
        data.forEach {
            with(view) {
                showData(it)
            }
        }
    }

    private fun initializePrediction() {
        with(view) {
            val selectedSensor = mPeripheryManager.getActiveSensor() ?: return
            selectedSensor.apply {
                hideNoStreamingMessage()
 /*               mChartsDataSubscription?.apply {
                    if (isDisposed) this.dispose()
                }
                mChartsDataSubscription = this.getDataFlowable()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSubscribe { startCharts(true) }
                        .subscribe {
                            showData(it)

                            if (predictionInProgress) {
                                return@subscribe
                            }

                            predictBuffer.add(it)
                            if (predictEnabled) {
                                predict()
                            }
                        }*/
            }
        }
    }

    private fun predict() {
        iUpdate++
        if (iUpdate >= updatesUntilPredictOnline || !predictOnlineEnabled) {
            iUpdate = 0
            doPredict()
        }
    }

    private fun doPredict() {
        if (predictBuffer.size == 8) {
            predictionInProgress = true
            val data = predictBuffer.flatMap { d -> d.asList() }.toFloatArray()
            val appContext = App.applicationComponent.getAppContext()
            val nextIntent = Intent(appContext, PredictionService::class.java).apply {
                type = PredictionService.ServiceCommands.PREDICT.name
                putExtra(PredictionService.PREDICT_DATA_KEY, data)
                putExtra(PredictionService.RECEIVER_KEY, predictionResultReceiver)
                putExtra(PredictionService.PREFER_OFFLINE_PREDICTION_KEY, !predictOnlineEnabled)
            }
            appContext.startService(nextIntent)
        }
    }

    private fun onPredictionResult(predictedClass: Int, distribution: FloatArray) {
        predictionInProgress = false

        val tryControl = control.guess(predictedClass)

        if (tryControl >= 0) {
            view.notifyPredict(
                    PredictResponse(listOf(Prediction(tryControl, distribution.asList()))))
            if (mPeripheryManager.motors != null) {
                val mot = mPeripheryManager.motors!!
                when (tryControl) {
                    0 -> mot.stopMotors()
                    1 -> mot.spinMotor(1, IMotors.FORWARD, 127)
                    2 -> mot.spinMotor(1, IMotors.BACKWARD, 127)
                    3 -> mot.spinMotor(2, IMotors.FORWARD, 127)
                    4 -> mot.spinMotor(2, IMotors.BACKWARD, 127)
                }
            }

        }
    }

    private fun onPredictionError(t: Throwable) {
        predictionInProgress = false
        view.notifyPredictError(t)
    }

    override fun onPredictSwitched(on: Boolean, predictOnline: Boolean) {
        predictEnabled = on
        predictOnlineEnabled = predictOnline
        predictBuffer = LimitedQueue(8)
        predictionInProgress = false
    }

    override fun destroy() {
        predictEnabled = false
        Sensor.removeSensorListener(TAG)
        view.startCharts(false)
        mChartsDataSubscription?.dispose()
        mDownloadModelSubscription?.dispose()
        mPostPredict?.dispose()
    }
}