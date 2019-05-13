package me.cyber.nukleos.ui.charts

import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import me.cyber.nukleos.ui.predict.PredictionService

class CalibrationPreparationResultReceiver(
        private val preparationResultCallback: (outputsSize: Int) -> Unit,
        private val errorCallback: (error: String) -> Unit
) :
        ResultReceiver(Handler()) {

    companion object {
        private const val DEFAULT_RESPONSE_ERROR = "Invalid calibration response"
    }

    override fun onReceiveResult(resultCode: Int, resultData: Bundle) {
        if (resultCode == PredictionService.ServiceResponses.SUCCESS.ordinal) {
            val outputsSize = resultData.getInt(PredictionService.CLASSES_COUNT_KEY, -1)
            preparationResultCallback(outputsSize)
        } else {
            val error = resultData.getString(PredictionService.ERROR_KEY)
            errorCallback(error ?: DEFAULT_RESPONSE_ERROR)
        }
    }
}