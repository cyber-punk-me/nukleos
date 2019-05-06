package me.cyber.nukleos.ui.predict

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import me.cyber.nukleos.App
import me.cyber.nukleos.IMotors
import me.cyber.nukleos.api.PredictRequest
import me.cyber.nukleos.api.PredictResponse
import me.cyber.nukleos.api.Prediction
import me.cyber.nukleos.control.TryControl
import me.cyber.nukleos.dagger.PeripheryManager
import me.cyber.nukleos.utils.LimitedQueue
import org.tensorflow.lite.Interpreter
import java.io.ByteArrayOutputStream
import java.lang.IllegalStateException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.zip.ZipInputStream

class PredictPresenter(override val view: PredictInterface.View, private val mPeripheryManager: PeripheryManager) : PredictInterface.Presenter(view) {

    companion object {
        private const val TFLITE_EXTENSION = ".tflite"
        private const val FLOAT_SIZE = 4
    }

    private var mChartsDataSubscription: Disposable? = null
    private var mDownloadModelSubscription: Disposable? = null
    private var mPostPredict: Disposable? = null
    private var predictEnabled = false
    private var predictOnlineEnabled = false
    private var predictBuffer = LimitedQueue<FloatArray>(8)
    private var iUpdate = 0
    private val updatesUntilPredict = 4
    private val control = TryControl()

    private lateinit var mInterpreter: Interpreter

    override fun create() {}

    override fun start() {
        mDownloadModelSubscription = getActualModel().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe({
                    mInterpreter = it
                    initializePrediction()
                }, { t ->
                    view.notifyPredictError(t)
                    initializePrediction()
                })
    }

    private fun initializePrediction() {
        with(view) {
            val selectedSensor = mPeripheryManager.getActiveSensor() ?: return
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

            //has downloaded tflite model
            if (::mInterpreter.isInitialized && !predictOnlineEnabled) {
                doLocalPredict(predictRequest)
            }
            else {
                doOnlinePredict(predictRequest)
            }
        }
    }

    private fun doLocalPredict(predictRequest: PredictRequest) {
        val outputTensorCount = mInterpreter.getOutputTensor(0).shape()[1]

        val data = predictRequest.instances[0]
        val byteBuffer = ByteBuffer.allocateDirect(data.size * FLOAT_SIZE)
        byteBuffer.order(ByteOrder.nativeOrder())
        data.forEach { byteBuffer.putFloat(it) }
        val result = Array(1) { FloatArray(outputTensorCount) }
        mInterpreter.run(byteBuffer, result)
        val distribution = result[0]
        onPredictionResult(distribution.indices.maxBy { distribution[it] } ?: -1, distribution.toList())
    }

    private fun doOnlinePredict(predictRequest: PredictRequest) {
        mPostPredict = App.applicationComponent.getApiHelper().api.predict(predictRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    val predictedClass = it.predictions[0].output
                    onPredictionResult(predictedClass, it.predictions[0].distr)
                }
                        , {
                    view.notifyPredictError(it)
                })
    }

    private fun onPredictionResult(predictedClass: Int, distribution: List<Float>) {
        val tryControl = control.guess(predictedClass)

        if (tryControl >= 0) {
            view.notifyPredict(
                    PredictResponse(listOf(Prediction(tryControl, distribution))))
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

    private fun getActualModel(): Observable<Interpreter> {
        val subject = BehaviorSubject.create<Interpreter>()

        mDownloadModelSubscription = App.applicationComponent.getApiHelper().api.downloadModel().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    try {
                        ZipInputStream(it.byteStream()).use { zipInputStream ->
                            var foundModel = false
                            while (!foundModel) {
                                val nextEntry = zipInputStream.nextEntry ?: break
                                if (!nextEntry.name.endsWith(TFLITE_EXTENSION, ignoreCase = true)) {
                                    continue
                                }

                                val buffer = ByteArray(1024)
                                ByteArrayOutputStream().use { outputStream ->
                                    while (true) {
                                        val read = zipInputStream.read(buffer, 0, 1024)
                                        if (read < 0) {
                                            break
                                        }
                                        outputStream.write(buffer, 0, read)
                                    }
                                    val bytes = outputStream.toByteArray()

                                    val byteBuffer = ByteBuffer.allocateDirect(bytes.size)
                                    byteBuffer.order(ByteOrder.nativeOrder())
                                    byteBuffer.put(bytes)
                                    val mInterpreter = Interpreter(byteBuffer)

                                    subject.onNext(mInterpreter)
                                    foundModel = true
                                }
                                break
                            }

                            if (!foundModel) {
                                subject.onError(IllegalStateException("Can't unzip downloaded model"))
                            }
                        }
                    } catch (t: Throwable) {
                        subject.onError(t)
                    }
                }
                        , {
                    subject.onError(it)
                })

        return subject
    }

    override fun onPredictSwitched(on: Boolean, predictOnline: Boolean) {
        predictEnabled = on
        predictOnlineEnabled = predictOnline
        predictBuffer = LimitedQueue(8)
    }

    override fun destroy() {
        view.startCharts(false)
        mChartsDataSubscription?.dispose()
        mDownloadModelSubscription?.dispose()
        mPostPredict?.dispose()
    }

}