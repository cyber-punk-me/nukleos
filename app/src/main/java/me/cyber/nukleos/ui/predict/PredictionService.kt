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
import org.deeplearning4j.nn.conf.NeuralNetConfiguration
import org.deeplearning4j.nn.conf.layers.DenseLayer
import org.deeplearning4j.nn.conf.layers.OutputLayer
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.nn.weights.WeightInit
import org.deeplearning4j.optimize.listeners.ScoreIterationListener
import org.nd4j.linalg.activations.Activation
import org.nd4j.linalg.cpu.nativecpu.NDArray
import org.nd4j.linalg.dataset.DataSet
import org.nd4j.linalg.learning.config.Nesterovs
import org.nd4j.linalg.lossfunctions.LossFunctions
import org.tensorflow.lite.Interpreter
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.zip.ZipInputStream

class PredictionService : IntentService(PredictionService::class.java.name) {

    companion object {
        const val CALIBRATION_DATA_KEY = "calibration_data"

        const val CLASSES_COUNT_KEY = "classes_count"

        const val ERROR_KEY = "error"
        const val PREDICTED_CLASS_KEY = "predicted_class"
        const val DISTRIBUTION_KEY = "distribution_key"

        const val RECEIVER_KEY = "receiver"
        const val PREDICT_DATA_KEY = "prediction_data"
        const val PREFER_OFFLINE_PREDICTION_KEY = "prefer_offline_prediction"

        private const val FLOAT_SIZE = 4

        private const val EXTERNAL_FILES_TYPE = "model"
        private const val TFLITE_MODEL_FILE_NAME = "model.tflite"
        private const val TFLITE_EXTENSION = ".tflite"

        private const val DL4J_MODEL_FILE_NAME = "calibration_network.dl4j"

        private val predictionServiceTag = PredictionService::class.java.name

        private const val rngSeed = 1337L

        private var bufferedTFModel: Interpreter? = null
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

            ServiceCommands.PREPARE_FOR_CALIBRATION -> {
                val savedModel = getSavedTfliteModel()
                if (savedModel == null) {
                    responseWithError(receiver, "Model is not saved")
                    return
                }
                val outputTensorCount = savedModel.getOutputTensor(1).shape()[1]
                onCalibrationPreparationResult(outputTensorCount, receiver)
            }

            ServiceCommands.CALIBRATE -> {
                @Suppress("UNCHECKED_CAST")
                val calibrationData = intent.getSerializableExtra(CALIBRATION_DATA_KEY) as? Array<Array<FloatArray>>
                if (calibrationData == null) {
                    responseWithError(receiver, "Failed to receive calibration data")
                    return
                }

                calibrate(calibrationData, receiver)
            }
            ServiceCommands.DELETE_SAVED_TF_MODEL -> {
                val modelGone = deleteSavedTfliteModel()
                receiver.send(modelGone.ordinal, Bundle.EMPTY)
            }
        }
    }

    private fun calibrate(calibrationData: Array<Array<FloatArray>>, receiver: ResultReceiver) {
        val savedModel = getSavedTfliteModel()
        if (savedModel == null) {
            responseWithError(receiver, "Model is not saved")
            return
        }

        try {
            val dataSet = getDataForCalibrationTraining(calibrationData, savedModel)
            val network = trainCoNetworkWithDeepLearning4j(dataSet)

            saveCalibrationNetworkToFile(network)
            onCalibrationResult(receiver)
        } catch (t: Throwable) {
            val message = t.message ?: ""
            Log.e(predictionServiceTag, message, t)
            responseWithError(receiver, message)
        }
    }

    private fun trainCoNetworkWithDeepLearning4j(dataSet: DataSet): MultiLayerNetwork {
        val conf = NeuralNetConfiguration.Builder()
                .seed(rngSeed) //include a random seed for reproducibility
                // use stochastic gradient descent as an optimization algorithm
                .updater(Nesterovs(0.006, 0.9))
                .l2(1e-4)
                .list()
                .layer(
                        DenseLayer.Builder() //create the first, input layer with xavier initialization
                                .nIn(20)
                                .nOut(11)
                                .activation(Activation.RELU)
                                .weightInit(WeightInit.XAVIER)
                                .build()
                )
                .layer(
                        OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD) //create hidden layer
                                .nIn(11)
                                .nOut(5)
                                .activation(Activation.SOFTMAX)
                                .weightInit(WeightInit.XAVIER)
                                .build()
                )
                .build()

        val model = MultiLayerNetwork(conf)
        model.init()
        //print the score with every 1 iteration
        model.setListeners(ScoreIterationListener(1))

        model.fit(dataSet)
        return model
    }

    private fun getDataForCalibrationTraining(calibrationData: Array<Array<FloatArray>>, interpreter: Interpreter): DataSet {
        val outputsSize = calibrationData.size

        val dataSets = mutableListOf<DataSet>()
        for (predictionClass in 0 until outputsSize) {
            val label = FloatArray(outputsSize).also { it[predictionClass] = 1f }
            val calibrationDataForClass = calibrationData[predictionClass]
            val midRepresentation = mutableListOf<FloatArray>()
            for (sample in calibrationDataForClass) {
                val tfOutput = predictWithInterpreter(sample.asList(), interpreter)
                midRepresentation.add((tfOutput[2] as Array<FloatArray>)[0])
            }

            val toTypedArray = (0 until midRepresentation.size).map { label.copyOf() }.toTypedArray()
            dataSets.add(DataSet(NDArray(midRepresentation.toTypedArray()), NDArray(toTypedArray)))
        }

        return DataSet.merge(dataSets)
    }

    private fun fetchCommand(requestTypeString: String): ServiceCommands? {
        return try {
            ServiceCommands.valueOf(requestTypeString)
        } catch (e: Exception) {
            Log.e(predictionServiceTag, e.message, e)
            null
        }
    }

    @Synchronized
    private fun predict(predictRequest: PredictRequest, receiver: ResultReceiver, preferOfflinePrediction: Boolean) {
        val interpreter = if (preferOfflinePrediction) getOrCreateTfliteModel() else null
        try {
            val prediction = if (preferOfflinePrediction && interpreter != null) {
                doLocalPredictWithTflite(predictRequest, interpreter)
            } else if (!preferOfflinePrediction) {
                doOnlinePredict(predictRequest)
            } else {
                null
            }

            //todo fix calibration
            //val finalPrediction = tryApplyCalibration(prediction)
            onPredictionResult(prediction, receiver)

        } catch (t: Throwable) {
            val message = t.message ?: ""
            "Failed to predict: $message".showShortToast()
            Log.e(predictionServiceTag, message, t)
            responseWithError(receiver, message)
        }
    }

    private fun doLocalPredictWithTflite(predictRequest: PredictRequest, interpreter: Interpreter): Prediction {
        val data = predictRequest.instances[0]
        val tfOutput = predictWithInterpreter(data, interpreter)
        return Prediction((tfOutput[0] as LongArray)[0].toInt(),
                (tfOutput[1] as Array<FloatArray>)[0].toList(),
                (tfOutput[2] as Array<FloatArray>)[0].toList())
    }

    private fun predictWithInterpreter(data: Collection<Float>, interpreter: Interpreter): Map<Int, Any> {
        val outputTensorCount = interpreter.getOutputTensor(1).shape()[1]
        val midTensorCount = interpreter.getOutputTensor(2).shape()[1]
        val byteBuffer = ByteBuffer.allocateDirect(data.size * FLOAT_SIZE)
        byteBuffer.order(ByteOrder.nativeOrder())
        data.forEach { byteBuffer.putFloat(it) }
        val result = mapOf(0 to LongArray(1),
                1 to arrayOf(FloatArray(outputTensorCount)),
                2 to arrayOf(FloatArray(midTensorCount)))
        try {
            interpreter.runForMultipleInputsOutputs(arrayOf(byteBuffer), result)
        } catch (t: Throwable) {
            Log.e(PredictionService::class.java.name, t.message, t)
        }
        return result
    }

    private fun doOnlinePredict(predictRequest: PredictRequest): Prediction {
        val predictResponse = App.applicationComponent.getApiHelper().getApi().predict(predictRequest).blockingGet()
        return predictResponse.predictions[0]
    }

    private fun onCalibrationPreparationResult(outputClassesCount: Int, receiver: ResultReceiver) {
        receiver.send(ServiceResponses.SUCCESS.ordinal, Bundle().apply {
            putInt(CLASSES_COUNT_KEY, outputClassesCount)
        })
    }

    private fun onCalibrationResult(receiver: ResultReceiver) {
        receiver.send(ServiceResponses.SUCCESS.ordinal, Bundle().apply {
        })
    }

    private fun onPredictionResult(prediction: Prediction?, receiver: ResultReceiver) {
        receiver.send(ServiceResponses.SUCCESS.ordinal, Bundle().apply {
            if (prediction != null) {
                putInt(PREDICTED_CLASS_KEY, prediction.output)
                putFloatArray(DISTRIBUTION_KEY, prediction.distr.toFloatArray())
            }
        })
    }

    private fun responseWithError(receiver: ResultReceiver, errorMessage: String) {
        receiver.send(ServiceResponses.ERROR.ordinal, Bundle().apply {
            putString(ERROR_KEY, errorMessage)
        })
    }

    private fun getSavedTfliteModel(): Interpreter? {
        if (bufferedTFModel != null) {
            return bufferedTFModel
        }
        Log.i(predictionServiceTag, "Loading saved TF model..")
        val modelFile = getTfliteModelLocation()

        if (modelFile.exists()) {
            try {
                bufferedTFModel = loadInterpreterFromFile(modelFile)
                Log.i(predictionServiceTag, "Saved TF model loaded.")
                return bufferedTFModel
            } catch (t: Throwable) {
                "Failed to load TF interpreter from file: ${t.message}".showShortToast()
                Log.e(predictionServiceTag, t.message, t)
            }
        }
        return null
    }

    //return if model does not exist after this method is executed
    private fun deleteSavedTfliteModel(): ServiceResponses {
        bufferedTFModel = null
        val modelFile = getTfliteModelLocation()

        return if (modelFile.exists()) {
            try {
                modelFile.delete()
                "Deleted TF model file ${modelFile.absolutePath}}"
                ServiceResponses.SUCCESS
            } catch (t: Throwable) {
                "Failed to delete TF model file ${modelFile.absolutePath}}"
                Log.e(predictionServiceTag, t.message, t)
                ServiceResponses.ERROR
            }
        } else {
            "Deleted TF model file ${modelFile.absolutePath}}"
            ServiceResponses.SUCCESS
        }
    }

    private fun getOrCreateTfliteModel(): Interpreter? {
        try {
            bufferedTFModel = getSavedTfliteModel()
            if (bufferedTFModel != null) {
                return bufferedTFModel
            }

            Log.i(predictionServiceTag, "Downloading TF model...")

            val responseBody = App.applicationComponent.getApiHelper().getApi().downloadModel().blockingGet()
            val byteArray = findTfliteModelInResponse(responseBody)
                    ?: throw java.lang.IllegalStateException("Can't download model from server")

            clearCalibrationNetwork()

            saveTfliteModelToFile(byteArray, getTfliteModelLocation())
            Log.i(predictionServiceTag, "TF Model Downloaded.")
            return loadInterpreterFromByteArray(byteArray)
        } catch (t: Throwable) {
            "Failed to download TF model: ${t.message}".showShortToast()
            Log.e(predictionServiceTag, t.message, t)
            return null
        }
    }

    private fun saveTfliteModelToFile(byteArray: ByteArray, modelFile: File) {
        modelFile.createNewFile()
        modelFile.writeBytes(byteArray)
    }

    private fun findTfliteModelInResponse(responseBody: ResponseBody): ByteArray? {
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

    private fun getTfliteModelLocation(): File {
        val externalFilesDir = getExternalFilesDir(EXTERNAL_FILES_TYPE)
        return File(externalFilesDir, TFLITE_MODEL_FILE_NAME)
    }

    private fun saveCalibrationNetworkToFile(network: MultiLayerNetwork) {
        network.save(getCalibrationNetworkLocation(), true)
    }

    private fun getCalibrationNetworkLocation(): File {
        val externalFilesDir = getExternalFilesDir(EXTERNAL_FILES_TYPE)
        return File(externalFilesDir, DL4J_MODEL_FILE_NAME)
    }

    private fun clearCalibrationNetwork() {
        getCalibrationNetworkLocation().deleteOnExit()
    }

    private fun getCalibrationNetwork(): MultiLayerNetwork? {
        val location = getCalibrationNetworkLocation()
        if (!location.exists()) {
            return null
        }
        return MultiLayerNetwork.load(location, true)
    }

    private fun tryApplyCalibration(predictionIn: Prediction): Prediction {
        if (predictionIn.midLayer == null) {
            return predictionIn
        }
        try {
            val network = getCalibrationNetwork() ?: return predictionIn
            val ndArray = NDArray(Array(1) { predictionIn.midLayer!!.toFloatArray() })
            val prediction = network.predict(ndArray)[0]
            return Prediction(prediction, List(predictionIn.distr.size) { 0f }) //TODO use weights from network
        } catch (t: Throwable) {
            Log.e(predictionServiceTag, t.message, t)
            return predictionIn
        }
    }

    enum class ServiceCommands {
        PREDICT,
        PREPARE_FOR_CALIBRATION,
        CALIBRATE,
        DELETE_SAVED_TF_MODEL
    }

    enum class ServiceResponses {
        SUCCESS,
        ERROR
    }
}
