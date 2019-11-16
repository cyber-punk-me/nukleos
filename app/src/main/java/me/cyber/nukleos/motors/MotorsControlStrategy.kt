package me.cyber.nukleos.motors

import android.util.Log
import me.cyber.nukleos.IMotors


class MotorsControlStrategy {

    fun control(motors: IMotors, dataClass: Int) {
        Log.d(MotorsBlueTooth.TAG, "Motors con : ${motors.isConnected()}; data : $dataClass")
    }


    companion object {
        const val TAG = "MotorsControlStrategy"
    }

}