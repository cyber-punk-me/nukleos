package me.cyber.nukleos.ui.training

import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import me.cyber.nukleos.ui.predict.PredictionService

class CalibrationResultReceiver(
        private val calibrationResultCallback: () -> Unit,
        private val errorCallback: (error: String) -> Unit
) :
        ResultReceiver(Handler()) {

    companion object {
        private const val DEFAULT_RESPONSE_ERROR = "Invalid calibration response"
    }

    override fun onReceiveResult(resultCode: Int, resultData: Bundle) {
        if (resultCode == PredictionService.ServiceResponses.SUCCESS.ordinal) {
            calibrationResultCallback()
        } else {
            val error = resultData.getString(PredictionService.ERROR_KEY)
            errorCallback(error ?: DEFAULT_RESPONSE_ERROR)
        }
    }
}