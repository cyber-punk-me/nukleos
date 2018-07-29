package me.cyber.nukleos.ui

import android.content.Intent
import android.databinding.ObservableField
import android.databinding.ObservableFloat
import android.databinding.ObservableInt
import com.thalmic.myo.Hub
import com.thalmic.myo.scanner.Scanner
import me.cyber.nukleos.App
import me.cyber.nukleos.R
import me.cyber.nukleos.extensions.*
import me.cyber.nukleos.services.ActionTypeEvent
import me.cyber.nukleos.services.SensorEvent
import me.cyber.nukleos.services.SensorWorkService
import me.cyber.nukleos.services.SensorWorkService.Companion.ActionType
import me.cyber.nukleos.services.SensorWorkService.Companion.EXTENSION
import me.cyber.nukleos.services.SensorWorkService.Companion.FLEXION
import me.cyber.nukleos.services.SensorWorkService.Companion.UNDEFINED
import me.cyber.nukleos.utils.EMPTY_STRING
import org.greenrobot.eventbus.Subscribe


class LearningViewModel : IBussed {

    val mScanStateText = ObservableField<String>("Scan state")

    val mFirstSensorValue = ObservableField<String>(R.string.value.getString())
    val mSecondSensorValue = ObservableField<String>(R.string.value.getString())
    val mThirdSensorValue = ObservableField<String>(R.string.value.getString())
    val mFourthSensorValue = ObservableField<String>(R.string.value.getString())
    val mFifthSensorValue = ObservableField<String>(R.string.value.getString())
    val mSixthSensorValue = ObservableField<String>(R.string.value.getString())
    val mSeventhSensorValue = ObservableField<String>(R.string.value.getString())
    val mEighthSensorValue = ObservableField<String>(R.string.value.getString())

    val mActionTypeText = ObservableField<String>(EMPTY_STRING)
    private var mActionType = UNDEFINED

    private val mHub by lazy {
        Hub.getInstance()
    }

    private val mHubScanner: Scanner by lazy {
        mHub.scanner
    }

    private val mContext by lazy {
        App.appComponent.getAppContext()
    }

    private val mSensorWorkService by lazy {
        Intent(mContext, SensorWorkService::class.java)
    }

    init {
        connectBus()
        mContext.startService(mSensorWorkService)
    }

    fun startExtension() {
        mScanStateText.set("Scanning....")
        mActionTypeText.set(getActionTypeText(EXTENSION))
        mHubScanner.startScanning()
    }

    fun startFlexion() {
        mScanStateText.set("Scanning....")
        mActionTypeText.set(getActionTypeText(FLEXION))
        mHubScanner.startScanning()
    }

    fun initSensor() = mContext.startService(mSensorWorkService)


    fun stopScan() = with(mHub) {
        mHubScanner.stopScanning()
        shutDown()
    }

    @Subscribe
    fun onEnableButton(event: SensorEvent) {
        event.data?.let { updateDisplayedValues(it) } ?: showConnectionError("Смотри в сервис. Нет коннекта с устройством")
    }

    private fun updateDisplayedValues(datalist: Array<Byte>) {
        mFirstSensorValue.set(datalist.get(0).toString())
        mSecondSensorValue.set(datalist.get(1).toString())
        mThirdSensorValue.set(datalist.get(2).toString())
        mFourthSensorValue.set(datalist.get(3).toString())
        mFifthSensorValue.set(datalist.get(4).toString())
        mSixthSensorValue.set(datalist.get(5).toString())
        mSeventhSensorValue.set(datalist.get(6).toString())
        mEighthSensorValue.set(datalist.get(7).toString())
    }

    private fun getActionTypeText(@ActionType actionType: Int): String {
        mActionType = actionType
        return when (actionType) {
            UNDEFINED -> "UNDEFINED"
            FLEXION -> "FLEXION"
            EXTENSION -> "EXTENSION"
            else -> "UNDEFINED"
        }
    }

    fun showConnectionError(errorMessage: String) = errorMessage.makeShortToast()

    fun release() {
        disconnectBus()
        mContext.stopService(mSensorWorkService)
    }

    fun shutDown() {
        mScanStateText.set("Scanning is complete. Send data to server.")
        ActionTypeEvent(mActionType).dispatch()
        mContext.stopService(mSensorWorkService)
    }
}