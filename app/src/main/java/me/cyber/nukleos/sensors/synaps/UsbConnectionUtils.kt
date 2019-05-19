package me.cyber.nukleos.sensors.synaps

import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.util.Log
import com.felhr.usbserial.CDCSerialDevice
import com.felhr.usbserial.UsbSerialDevice
import com.felhr.usbserial.UsbSerialInterface
import me.cyber.nukleos.App
import me.cyber.nukleos.sensors.synaps.UsbService.TAG

fun connect(device: UsbDevice, usbManager: UsbManager, onSensorInit: (UsbSensor) -> Unit) {
    val usbHandler = UsbHandler()
    ConnectionThread(device, usbManager, usbHandler, onSensorInit).start()
}

/*
 * A simple thread to open a serial port.
 * Although it should be a fast operation. moving usb operations away from UI thread is a good thing.
 */
private class ConnectionThread internal constructor(
        private val device: UsbDevice,
        private val usbManager: UsbManager,
        private val usbHandler: UsbHandler,
        private val onSensorInit: (UsbSensor) -> Unit) : Thread() {

    override fun run() {
        val connection = usbManager.openDevice(device)
        val serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection)

        if (serialPort.syncOpen()) {
            serialPort.setBaudRate(UsbSensor.BAUD_RATE)
            serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8)
            serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1)
            serialPort.setParity(UsbSerialInterface.PARITY_NONE)
            /*
             * Current flow control Options:
             * UsbSerialInterface.FLOW_CONTROL_OFF
             * UsbSerialInterface.FLOW_CONTROL_RTS_CTS only for CP2102 and FT232
             * UsbSerialInterface.FLOW_CONTROL_DSR_DTR only for CP2102 and FT232
             */
            serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF)
            usbHandler.registerCallbacks(serialPort)

            ReadThread(serialPort, usbHandler).start()

            //
            // Some Arduinos would need some sleep because firmware wait some time to know whether a new sketch is going
            // to be uploaded or not
            //Thread.sleep(2000); // sleep some. YMMV with different chips.

            // Everything went as expected. Send an intent to MainActivity
            val intent = Intent(UsbService.ACTION_USB_READY)
            App.applicationComponent.getAppContext().sendBroadcast(intent)

            Thread {
                UsbSensor.startStreaming(serialPort)
                onSensorInit(UsbSensor(usbHandler, serialPort))
            }.start()
        } else {
            // Serial port could not be opened, maybe an I/O error or if CDC driver was chosen, it does not really fit
            // Send an Intent to Main Activity
            val intent = Intent(
                    if (serialPort is CDCSerialDevice)
                        UsbService.ACTION_CDC_DRIVER_NOT_WORKING
                    else
                        UsbService.ACTION_USB_DEVICE_NOT_WORKING
            )
            App.applicationComponent.getAppContext().sendBroadcast(intent)
        }
    }
}

private const val READ_TIMEOUT = 1000

private class ReadThread(
        private val serialPort: UsbSerialDevice,
        private val handler: UsbHandler) : Thread() {

    override fun run() {
        while (true) {
            if (!serialPort.isOpen) {
                Log.w(TAG, "Usb Sensor closed connection.")
                break
            }
            val buffer = ByteArray(1000)
            val n = serialPort.syncRead(buffer, READ_TIMEOUT)
            if (n > 0) {
                val received = ByteArray(n)
                System.arraycopy(buffer, 0, received, 0, n)
                handler.obtainMessage(UsbService.SYNC_READ, received).sendToTarget()
                if (n >= 100) {
                    Log.w(TAG, "got plenty bytes: $n")
                }
            }
        }
    }
}
