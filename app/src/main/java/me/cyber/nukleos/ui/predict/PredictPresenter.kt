package me.cyber.nukleos.ui.predict

import android.content.Intent
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.cyber.nukleos.App
import me.cyber.nukleos.api.PredictResponse
import me.cyber.nukleos.api.Prediction
import me.cyber.nukleos.control.ControlManager
import me.cyber.nukleos.dagger.PeripheryManager
import me.cyber.nukleos.data.mapNeuralDefault
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
    private val control = ControlManager()

    @Volatile
    private var predictionInProgress = false

    override fun create() {}

    override fun start() {
        Sensor.registerSensorListener(TAG, this)

        control.reset()

        control.addControlListener(TAG, object : ControlManager.ControlListener {
            override fun onMotionUpdated(dataClass: Int) {
                mPeripheryManager.onMotionUpdated(dataClass)
            }
        })

        with(view) {
            startCharts(true)
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
        GlobalScope.launch {
            if (!predictionInProgress) {
                predictionInProgress = true
                val transformed = mapNeuralDefault(data)
                val appContext = App.applicationComponent.getAppContext()
                val nextIntent = Intent(appContext, PredictionService::class.java).apply {
                    type = PredictionService.ServiceCommands.PREDICT.name
                    putExtra(PredictionService.PREDICT_DATA_KEY, transformed.flatMap { d -> d.flatMap { it.toList() } }.toFloatArray())
                    putExtra(PredictionService.RECEIVER_KEY, predictionResultReceiver)
                    putExtra(PredictionService.PREFER_OFFLINE_PREDICTION_KEY, !predictOnlineEnabled)
                }
                appContext.startService(nextIntent)
            }
        }
    }

    private fun onPredictionResult(predictedClass: Int, distribution: FloatArray) {
        predictionInProgress = false
        if (predictEnabled) {
            control.notifyDataArrived(predictedClass)
            view.notifyPredict(
                PredictResponse(listOf(Prediction(predictedClass, distribution.asList()))))
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
            Sensor.registerSensorListener(PREDICTION_TAG, object : SensorListener {
                override fun onSensorData(sensorName: String, data: List<FloatArray>) {
                    if (predictEnabled && !predictionInProgress) {
                        predict(data)
                    }
                }

            }, SubscriptionParams(8, 8))
        } else {
            Sensor.removeSensorListener(PREDICTION_TAG)
        }
        view.notifyPredictEnabled(predictEnabled)
    }

    override fun destroy() {
        predictEnabled = false
        view.notifyPredictEnabled(predictEnabled)
        Sensor.removeSensorListener(TAG)
        control.removeControlListener(TAG)
        view.startCharts(false)
        mPostPredict?.dispose()
    }
}