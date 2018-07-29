package me.cyber.nukleos.sensor

import com.thalmic.myo.Hub
import me.cyber.nukleos.App
import me.cyber.nukleos.R
import me.cyber.nukleos.extensions.getString

class Sensor : ISensor, ISensorSettings {


    companion object {
        fun getInstance() = Sensor()
    }

    private val mOutsideSensor by lazy {
        Hub.getInstance()
    }

    override fun addListener(listener: ISensorListener) {
    }
    override fun init() = mOutsideSensor.init(App.appComponent.getAppContext(), R.string.application_identifier.getString())
    override fun shutdown() = mOutsideSensor.shutdown()
}