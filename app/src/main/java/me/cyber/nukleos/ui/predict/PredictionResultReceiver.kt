package me.cyber.nukleos.ui.predict

import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver

class PredictionResultReceiver(
        handler: Handler,
        private val predictionCallback: (predictedClass: Int, distribution: FloatArray) -> Unit,
        private val errorCallback: (error: String) -> Unit
) :
        ResultReceiver(handler) {

    companion object {
        private const val DEFAULT_PREDICTION_RESPONSE_ERROR = "Invalid prediction response"
    }

    override fun onReceiveResult(resultCode: Int, resultData: Bundle) {
        if (resultCode == PredictionService.ServiceResponses.SUCCESS.ordinal) {
            val predictedClass = resultData.getInt(PredictionService.PREDICTED_CLASS_KEY)
            val distribution = resultData.getFloatArray(PredictionService.DISTRIBUTION_KEY)
            if (distribution == null) {
                errorCallback(DEFAULT_PREDICTION_RESPONSE_ERROR)
                return
            }
            predictionCallback(predictedClass, distribution)
        } else {
            val error = resultData.getString(PredictionService.ERROR_KEY)
            errorCallback(error ?: DEFAULT_PREDICTION_RESPONSE_ERROR)
        }
    }
}