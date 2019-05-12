package me.cyber.nukleos.ui.predict

import android.app.IntentService
import android.content.Intent
import android.os.Bundle
import android.os.ResultReceiver
import android.util.Log
import me.cyber.nukleos.App
import me.cyber.nukleos.api.PredictRequest
import me.cyber.nukleos.api.Prediction
import me.cyber.nukleos.utils.showShortToast
import okhttp3.ResponseBody
import org.apache.commons.io.IOUtils
import org.tensorflow.lite.Interpreter
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.zip.ZipInputStream

class PredictionService : IntentService(PredictionService::class.java.name) {

    companion object {
        const val ERROR_KEY = "error"
        const val PREDICTED_CLASS_KEY = "predicted_class"
        const val DISTRIBUTION_KEY = "distribution_key"

        const val RECEIVER_KEY = "receiver"
        const val PREDICT_DATA_KEY = "prediction_data"
        const val PREFER_OFFLINE_PREDICTION_KEY = "prefer_offline_prediction"

        private const val FLOAT_SIZE = 4

        private const val EXTERNAL_FILES_TYPE = "model"
        private const val MODEL_FILE_NAME = "model.tflite"
        private const val TFLITE_EXTENSION = ".tflite"

        private val predictionServiceTag = PredictionService::class.java.name
    }

    override fun onHandleIntent(intent: Intent) {
        val receiver = intent.extras?.get(RECEIVER_KEY) as? ResultReceiver
        if (receiver == null) {
            Log.e(predictionServiceTag, "Request doesn't contain result receiver")
            return
        }

        val requestTypeString = intent.type
        if (requestTypeString == null) {
            Log.e(predictionServiceTag, "Intent type should be declared")
            return
        }

        val requestType = fetchCommand(requestTypeString) ?: return

        when (requestType) {

            ServiceCommands.PREDICT -> {
                val predictionData = intent.getFloatArrayExtra(PREDICT_DATA_KEY)
                if (predictionData == null) {
                    Log.e(predictionServiceTag, "Intent type should be declared")
                    return
                }
                val preferOfflinePrediction = intent.getBooleanExtra(PREFER_OFFLINE_PREDICTION_KEY, true)
                predict(PredictRequest(ArrayList<List<Float>>().also { it.add(predictionData.asList()) }), receiver, preferOfflinePrediction)
            }
            ServiceCommands.CALIBRATE -> TODO()
            ServiceCommands.UPDATE_MODEL -> TODO()
        }
    }

    private fun fetchCommand(requestTypeString: String) : ServiceCommands? {
        return try {
            ServiceCommands.valueOf(requestTypeString)
        } catch (e: Exception) {
            Log.e(predictionServiceTag, e.message, e)
            null
        }
    }

    private fun predict(predictRequest: PredictRequest, receiver: ResultReceiver, preferOfflinePrediction: Boolean) {
        val interpreter = if (preferOfflinePrediction) getOrCreateModel() else null
        try {
            val prediction = if (interpreter != null) {
                doLocalPredict(predictRequest, interpreter)
            } else {
                doOnlinePredict(predictRequest)
            }
            onPredictionResult(prediction, receiver)

        } catch (t: Throwable) {
            val message = t.message ?: ""
            "Failed to predict: $message".showShortToast()
            Log.e(predictionServiceTag, message, t)
            onPredictionError(receiver, message)
        }
    }

    private fun doLocalPredict(predictRequest: PredictRequest, interpreter: Interpreter) : Prediction {
        val outputTensorCount = interpreter.getOutputTensor(0).shape()[1]

        val data = predictRequest.instances[0]
        val byteBuffer = ByteBuffer.allocateDirect(data.size * FLOAT_SIZE)
        byteBuffer.order(ByteOrder.nativeOrder())
        data.forEach { byteBuffer.putFloat(it) }
        val result = Array(1) { FloatArray(outputTensorCount) }
        interpreter.run(byteBuffer, result)
        val distribution = result[0]

        return Prediction(distribution.indices.maxBy { distribution[it] }
                ?: -1, distribution.toList())
    }

    private fun doOnlinePredict(predictRequest: PredictRequest): Prediction {
        val predictResponse = App.applicationComponent.getApiHelper().api.predict(predictRequest).blockingGet()
        return predictResponse.predictions[0]
    }

    private fun onPredictionResult(prediction: Prediction, receiver: ResultReceiver) {
        receiver.send(ServiceResponses.SUCCESS.ordinal, Bundle().apply {
            putInt(PREDICTED_CLASS_KEY, prediction.output)
            putFloatArray(DISTRIBUTION_KEY, prediction.distr.toFloatArray())
        })
    }

    private fun onPredictionError(receiver: ResultReceiver, errorMessage: String) {
        receiver.send(ServiceResponses.ERROR.ordinal, Bundle().apply {
            putString(ERROR_KEY, errorMessage)
        })
    }

    private fun getOrCreateModel(): Interpreter? {
        try {
            val modelFile = getModelLocation()

            if (modelFile.exists()) {
                try {
                    return loadInterpreterFromFile(modelFile)
                } catch (t: Throwable) {
                    "Failed to load interpreter from file: ${t.message}".showShortToast()
                    Log.e(predictionServiceTag, t.message, t)
                }
            }

            val responseBody = App.applicationComponent.getApiHelper().api.downloadModel().blockingGet()
            val byteArray = findModelInResponse(responseBody)
                    ?: throw java.lang.IllegalStateException("Can't download model from server")

            saveModelToFile(byteArray, modelFile)
            return loadInterpreterFromByteArray(byteArray)
        } catch (t: Throwable) {
            "Failed to get tflite model: ${t.message}. Fallback to online prediction".showShortToast()
            Log.e(predictionServiceTag, t.message, t)
            return null
        }
    }

    private fun saveModelToFile(byteArray: ByteArray, modelFile: File) {
        modelFile.createNewFile()
        modelFile.writeBytes(byteArray)
    }

    private fun findModelInResponse(responseBody: ResponseBody): ByteArray? {
        ZipInputStream(responseBody.byteStream()).use { zipInputStream ->
            while (true) {
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
                    return outputStream.toByteArray()
                }
            }
        }

        return null
    }

    private fun loadInterpreterFromFile(modelFile: File): Interpreter {
        return loadInterpreterFromByteArray(IOUtils.toByteArray(modelFile.inputStream()))
    }

    private fun loadInterpreterFromByteArray(byteArray: ByteArray): Interpreter {
        val byteBuffer = ByteBuffer.allocateDirect(byteArray.size)
        byteBuffer.order(ByteOrder.nativeOrder())
        byteBuffer.put(byteArray)
        return Interpreter(byteBuffer)
    }

    private fun getModelLocation(): File {
        val externalFilesDir = getExternalFilesDir(EXTERNAL_FILES_TYPE)
        return File(externalFilesDir, MODEL_FILE_NAME)
    }

    enum class ServiceCommands {
        PREDICT,
        CALIBRATE,
        UPDATE_MODEL
    }

    enum class ServiceResponses {
        SUCCESS,
        ERROR
    }
}
