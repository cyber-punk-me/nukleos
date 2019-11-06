package me.cyber.nukleos.ui.predict

import android.content.Intent
import io.reactivex.disposables.Disposable
import me.cyber.nukleos.App
import me.cyber.nukleos.api.PredictResponse
import me.cyber.nukleos.api.Prediction
import me.cyber.nukleos.control.TryControl
import me.cyber.nukleos.dagger.PeripheryManager
import me.cyber.nukleos.sensors.Sensor
import me.cyber.nukleos.sensors.SensorListener
import me.cyber.nukleos.sensors.SubscriptionParams

class PredictPresenter(override val view: PredictInterface.View, private val mPeripheryManager: PeripheryManager)
    : PredictInterface.Presenter(view), SensorListener {

    private val TAG = "PredictPresenter"
    private val PREDICTION_TAG = "PREDICTION"

    private val predictionResultReceiver = PredictionResultReceiver(
            { predictedClass, distribution ->
                onPredictionResult(predictedClass, distribution)
            },
            { onPredictionError(Exception(it)) })
    private var mPostPredict: Disposable? = null
    private var predictEnabled = false
    private var predictOnlineEnabled = false
    private val control = TryControl()

    @Volatile
    private var predictionInProgress = false

    override fun create() {}

    override fun start() {
        Sensor.registerSensorListener(TAG, this)
        with(view) {
            startCharts(true)
        }
        with(view) {
            val selectedSensor = mPeripheryManager.getActiveSensor() ?: return
            selectedSensor.apply {
                hideNoStreamingMessage()
            }
        }
    }

    override fun onSensorData(sensorName: String, data: List<FloatArray>) {
        with(view) {
            showData(data)
        }
    }

    private fun predict(data: List<FloatArray>) {
        if (!predictionInProgress) {
            predictionInProgress = true
            val appContext = App.applicationComponent.getAppContext()
            val nextIntent = Intent(appContext, PredictionService::class.java).apply {
                type = PredictionService.ServiceCommands.PREDICT.name
                putExtra(PredictionService.PREDICT_DATA_KEY, data.flatMap { d -> d.asList() }.toFloatArray())
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
                    1 -> mot.spinMotor(0, 127)
                    2 -> mot.spinMotor(0, -127)
                    3 -> mot.spinMotor(1, 127)
                    4 -> mot.spinMotor(1, -127)
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
        predictionInProgress = false
        if (on) {
            Sensor.registerSensorListener(PREDICTION_TAG, object : SensorListener{
                override fun onSensorData(sensorName: String, data: List<FloatArray>) {
                    if (predictEnabled && !predictionInProgress) {
                        predict(data)
                    }
                }

            }, SubscriptionParams(8, 2))
        } else {
            Sensor.removeSensorListener(PREDICTION_TAG)
        }
    }

    override fun destroy() {
        predictEnabled = false
        Sensor.removeSensorListener(TAG)
        view.startCharts(false)
        mPostPredict?.dispose()
    }
}