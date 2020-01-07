package me.cyber.nukleos.ui.training

import android.content.Intent
import android.util.Log
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.cyber.nukleos.App
import me.cyber.nukleos.dagger.PeripheryManager
import me.cyber.nukleos.data.DEFAULT_DATA_READS_PER_FEATURE
import me.cyber.nukleos.data.mapNeuralDefault
import me.cyber.nukleos.sensors.Sensor
import me.cyber.nukleos.sensors.SensorListener
import me.cyber.nukleos.ui.training.TrainingFragment.Companion.LEARNING_TIME
import me.cyber.nukleos.ui.predict.PredictionService
import me.cyber.nukleos.utils.showShortToast
import java.lang.StringBuilder
import java.util.*
import kotlin.collections.ArrayList

class TrainingPresenter(override val view: TrainingInterface.View, private val mPeripheryManager: PeripheryManager)
    : TrainingInterface.Presenter(view), SensorListener {

    private val TAG = "TrainingPresenter"
    private val mApi by lazy { App.applicationComponent.getApiHelper().getApi() }
    private val mLearningSessId = UUID.fromString("885d0665-ca5d-46ed-b6dc-ea2c2610a67f")
    private val mScriptId = UUID.fromString("7de76908-d4d9-4ce9-98de-118a4fb3b8f8")
    private var mServerTimeMinusLocal: Long = 0

    private var mPostDataSubscription: Disposable? = null
    private var mTrainModelSubscription: Disposable? = null
    private var mServerTimeSubscription: Disposable? = null

    private val calibrationPreparationResultReceiver = CalibrationPreparationResultReceiver(
            {
                if (it <= 0) {
                    "Wrong classes count: $it".showShortToast()
                    return@CalibrationPreparationResultReceiver
                }
                val data = ArrayList<Array<FloatArray>>()
                collectDataForClass(0, it, data)
            }, {
        it.showShortToast()
    })
    private val calibrationResultReceiver = CalibrationResultReceiver(
            {
                "Calibration is finished".showShortToast()
                view.goToState(TrainingInterface.State.IDLE)
            }, {
        it.showShortToast()
        view.goToState(TrainingInterface.State.IDLE)
    })
    private val deleteTfModelResultReceiver = DeleteModelResultReceiver(
            {
                "Model deleted".showShortToast()
            },
            {
                "Model delete failed".showShortToast()
            })

    private fun convertData(data: List<FloatArray>, dataClass: Int): String {
        val builder = StringBuilder()

        val grouped = mapNeuralDefault(data)

        grouped.forEach { timeGroup ->
            timeGroup.forEach { sensorFeatures ->
                sensorFeatures.forEach {
                    builder.append("$it,")
                }
            }
            builder.append("$dataClass\n")
        }

        val result = builder.toString()
        return result
    }

    private fun sendData(data: String, learningSessId: UUID) = with(view) {
        mPostDataSubscription = mApi.postData(learningSessId, data, ".csv")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    goToState(TrainingInterface.State.IDLE)
                    notifyDataSent()
                }, {
                    goToState(TrainingInterface.State.IDLE)
                    notifyDataFailed()
                })
    }

    private fun trainModel(dataId: UUID, scriptId: UUID) {
        //delete previously saved model outdated
        val appContext = App.applicationComponent.getAppContext()
        val nextIntent = Intent(appContext, PredictionService::class.java).apply {
            type = PredictionService.ServiceCommands.DELETE_SAVED_TF_MODEL.name
            putExtra(PredictionService.RECEIVER_KEY, deleteTfModelResultReceiver)
        }
        appContext.startService(nextIntent)
        //train model online
        mTrainModelSubscription = mApi.trainModel(dataId, scriptId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    view.goToState(TrainingInterface.State.IDLE)
                    view.notifyTrainModelStarted(it)
                }, {
                    view.goToState(TrainingInterface.State.IDLE)
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

    override fun create() {}

    override fun onSensorData(sensorName: String, data: List<FloatArray>) {
        with(view) {
            showData(data)
        }
    }

    override fun start() {
        Sensor.registerSensorListener(TAG, this)
        with(view) {
            val firstStreamingSensor = mPeripheryManager.getActiveSensor()
            if (firstStreamingSensor == null) {
                showNoStreamingMessage()
                return
            }
            hideNoStreamingMessage()
            startCharts(true)
        }
        getServerTimeDiff()
    }

    override fun onCollectPressed() {
        collectData(LEARNING_TIME) {
            view.goToState(TrainingInterface.State.SENDING)
            val dataToSend = convertData(it,
                    view.getDataType())
            sendData(dataToSend, mLearningSessId)
        }
    }

    override fun onCollectStarted(dataWindow: Int, onCollected: (List<FloatArray>) -> Unit) {
        Sensor.listenOnce(object : SensorListener {
            override fun onSensorData(sensorName: String, data: List<FloatArray>) {
                GlobalScope.launch {
                    onCollected(data)
                }
            }
        }, dataWindow)
    }

    private fun collectData(collectTimeSeconds: Int, onCollected: (List<FloatArray>) -> Unit) {
        with(view) {
            val selectedSensor = mPeripheryManager.getActiveSensor()
            if (selectedSensor == null) {
                showNoStreamingMessage()
                return
            }
            val dataWindow = collectTimeSeconds * selectedSensor.getFrequency()
            selectedSensor.apply {
                goToState(TrainingInterface.State.COUNTDOWN, dataWindow, onCollected)
                hideNoStreamingMessage()
            }
        }
    }

    override fun onTrainPressed() = trainModel(mLearningSessId, mScriptId)

    override fun onCalibratePressed() {
        startPreparationForCalibration()
    }

    private fun startPreparationForCalibration() {
        val appContext = App.applicationComponent.getAppContext()
        val nextIntent = Intent(appContext, PredictionService::class.java).apply {
            type = PredictionService.ServiceCommands.PREPARE_FOR_CALIBRATION.name
            putExtra(PredictionService.RECEIVER_KEY, calibrationPreparationResultReceiver)
        }
        appContext.startService(nextIntent)
    }

    private fun collectDataForClass(classNumber: Int, totalNumberOfClasses: Int, data: ArrayList<Array<FloatArray>>) {
        view.setDataType(classNumber)
        collectData(LEARNING_TIME) {

            @Suppress("NestedLambdaShadowedImplicitParameter")
            val classData = it.withIndex()
                    .groupBy { it.index / DEFAULT_DATA_READS_PER_FEATURE } //make groups of 8 x 8 floats
                    .map { it.value.map { it.value } } //get rid of indices
                    .dropLast(1) //drop last group that could be incomplete
                    .map { it.flatMap { it.toList() }.toFloatArray() } //make arrays of 64 floats for prediction purposes
                    .toTypedArray()
            data.add(classData)
            if (classNumber >= totalNumberOfClasses - 1) {
                sendDataForCalibration(data.toTypedArray())
            } else {
                collectDataForClass(classNumber + 1, totalNumberOfClasses, data)
            }
        }
    }

    private fun sendDataForCalibration(data: Array<Array<FloatArray>>) {
        val appContext = App.applicationComponent.getAppContext()
        val nextIntent = Intent(appContext, PredictionService::class.java).apply {
            type = PredictionService.ServiceCommands.CALIBRATE.name
            putExtra(PredictionService.RECEIVER_KEY, calibrationResultReceiver)
            putExtra(PredictionService.CALIBRATION_DATA_KEY, data)
        }
        appContext.startService(nextIntent)
    }

    override fun destroy() {
        view.startCharts(false)
        Sensor.removeSensorListener(TAG)
        mPostDataSubscription?.dispose()
        mServerTimeSubscription?.dispose()
    }
}