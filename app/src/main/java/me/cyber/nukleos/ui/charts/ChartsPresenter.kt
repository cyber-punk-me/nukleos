package me.cyber.nukleos.ui.charts

import android.content.Intent
import android.util.Log
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import me.cyber.nukleos.App
import me.cyber.nukleos.dagger.PeripheryManager
import me.cyber.nukleos.sensors.Sensor
import me.cyber.nukleos.sensors.SensorListener
import me.cyber.nukleos.ui.charts.ChartsFragment.Companion.LEARNING_TIME
import me.cyber.nukleos.ui.charts.ChartsFragment.Companion.TIMER_COUNT
import me.cyber.nukleos.ui.predict.PredictionService
import me.cyber.nukleos.utils.showShortToast
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class ChartsPresenter(override val view: ChartInterface.View, private val mPeripheryManager: PeripheryManager)
    : ChartInterface.Presenter(view), SensorListener {

    private val TAG = "ChartsPresenter"
    private val millsBetweenReads = 5
    private val numSensors = 8
    private val mApi by lazy { App.applicationComponent.getApiHelper().getApi() }
    private val mLearningSessId = UUID.fromString("885d0665-ca5d-46ed-b6dc-ea2c2610a67f")
    private val mScriptId = UUID.fromString("7de76908-d4d9-4ce9-98de-118a4fb3b8f8")
    private var mServerTimeMinusLocal: Long = 0

    private var mDataSubscription: Disposable? = null
    private var mChartsDataSubscription: Disposable? = null
    private var mPostDataSubscription: Disposable? = null
    private var mTrainModelSubscription: Disposable? = null
    private var mServerTimeSubscription: Disposable? = null
    private var mMarkTime = false

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
                view.goToState(ChartInterface.State.IDLE)
            }, {
        it.showShortToast()
        view.goToState(ChartInterface.State.IDLE)
    })
    private val deleteTfModelResultReceiver = DeleteModelResultReceiver(
            {
                "Model deleted".showShortToast()
            },
            {
                "Model delete failed".showShortToast()
            })

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

    override fun onSensorData(data: FloatArray) {
        with(view) {
            showData(data)
        }
    }

    override fun start() {
        Sensor.registerSensorListener(TAG, this)
        with(view) {
            //TODO we should support several sensors
            val firstStreamingSensor = mPeripheryManager.getActiveSensor()
            if (firstStreamingSensor == null) {
                showNoStreamingMessage()
                return
            }

            hideNoStreamingMessage()
/*            mChartsDataSubscription?.apply {
                if (isDisposed) this.dispose()
            }
            mChartsDataSubscription = firstStreamingSensor.getDataFlowable()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe { startCharts(true) }
                    .subscribe { showData(it) }*/

            startCharts(true)

        }
        getServerTimeDiff()
    }

    override fun onCollectPressed() {
        collectData(LEARNING_TIME) {
            view.goToState(ChartInterface.State.SENDING)
            sendData(convertData(it,
                    view.getDataType(), 64, 64),
                    mLearningSessId)
        }
    }

    private fun collectData(collectTimeSeconds: Int, onCollected: (ArrayList<FloatArray>) -> Unit) {
        val dataBuffer: ArrayList<FloatArray> = arrayListOf()
        with(view) {
            val selectedSensor = mPeripheryManager.getActiveSensor()
            if (selectedSensor == null) {
                showNoStreamingMessage()
                return
            }
            selectedSensor.apply {
                goToState(ChartInterface.State.COUNTDOWN)
                hideNoStreamingMessage()
                mDataSubscription?.dispose()
                mDataSubscription = this.getDataFlowable()
                        .skip(TIMER_COUNT, TimeUnit.SECONDS)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .take(TIMER_COUNT + collectTimeSeconds, TimeUnit.SECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnComplete {
                            onCollected(dataBuffer)
                        }
                        .subscribe {
                            dataBuffer.add(it)
                        }
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
                    .groupBy { it.index / 8 } //make groups of 8 x 8 floats
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
        mDataSubscription?.dispose()
        mChartsDataSubscription?.dispose()
        mPostDataSubscription?.dispose()
        mServerTimeSubscription?.dispose()
    }
}