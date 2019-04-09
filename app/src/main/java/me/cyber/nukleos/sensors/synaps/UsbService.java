package me.cyber.nukleos.sensors.synaps;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.felhr.usbserial.UsbSerialDevice;

import java.util.HashMap;
import java.util.Map;

import kotlin.Unit;
import me.cyber.nukleos.dagger.PeripheryManager;
import me.cyber.nukleus.R;

import static me.cyber.nukleos.sensors.synaps.UsbConnectionUtilsKt.connect;

/**
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2014 Felipe Herranz
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

public class UsbService extends Service {

    public static final String TAG = "UsbService";

    public static final String ACTION_USB_READY = "com.felhr.connectivityservices.USB_READY";
    public static final String ACTION_USB_ATTACHED = "android.hardware.usb.action.USB_DEVICE_ATTACHED";
    public static final String ACTION_USB_DETACHED = "android.hardware.usb.action.USB_DEVICE_DETACHED";
    public static final String ACTION_USB_NOT_SUPPORTED = "com.felhr.usbservice.USB_NOT_SUPPORTED";
    public static final String ACTION_NO_USB = "com.felhr.usbservice.NO_USB";
    public static final String ACTION_USB_PERMISSION_GRANTED = "com.felhr.usbservice.USB_PERMISSION_GRANTED";
    public static final String ACTION_USB_PERMISSION_NOT_GRANTED = "com.felhr.usbservice.USB_PERMISSION_NOT_GRANTED";
    public static final String ACTION_USB_DISCONNECTED = "com.felhr.usbservice.USB_DISCONNECTED";
    public static final String ACTION_CDC_DRIVER_NOT_WORKING = "com.felhr.connectivityservices.ACTION_CDC_DRIVER_NOT_WORKING";
    public static final String ACTION_USB_DEVICE_NOT_WORKING = "com.felhr.connectivityservices.ACTION_USB_DEVICE_NOT_WORKING";
    public static final int MESSAGE_FROM_SERIAL_PORT = 0;
    public static final int CTS_CHANGE = 1;
    public static final int DSR_CHANGE = 2;
    public static final int SYNC_READ = 3;
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    public static boolean SERVICE_CONNECTED = false;

    private static final String USB_DEVICE_EXTRA_KEY = "usb_device";

    private IBinder binder = new UsbBinder();

    private UsbManager usbManager;

    public PeripheryManager peripheryManager;

    /*
     * Different notifications from OS will be received here (USB attached, detached, permission responses...)
     * About BroadcastReceiver: http://developer.android.com/reference/android/content/BroadcastReceiver.html
     */
    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            String action = arg1.getAction();
            if (action == null) {
                String intentIsMissingError = getString(R.string.intent_is_missing);
                Log.e(intentIsMissingError, intentIsMissingError);
                return;
            }
            switch (action) {
                case ACTION_USB_PERMISSION:
                    Bundle extras = arg1.getExtras();
                    if (extras == null) {
                        Log.e(TAG, getString(R.string.extras_is_missing));
                        return;
                    }
                    boolean granted = extras.getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
                    if (granted) // User accepted our USB connection. Try to open the device as a serial port
                    {
                        Intent intent = new Intent(ACTION_USB_PERMISSION_GRANTED);
                        arg0.sendBroadcast(intent);
                        UsbDevice device = extras.getParcelable(USB_DEVICE_EXTRA_KEY);
                        if (device == null) {
                            Log.e(TAG, getString(R.string.usb_device_is_not_provided));
                            return;
                        }

                        connect(device, usbManager, sensor -> {
                            if (peripheryManager != null)
                                peripheryManager.addSensor(sensor);
                            return Unit.INSTANCE;
                        });
                    } else // User not accepted our USB connection. Send an Intent to the Main Activity
                    {
                        Intent intent = new Intent(ACTION_USB_PERMISSION_NOT_GRANTED);
                        arg0.sendBroadcast(intent);
                    }
                    break;
                case ACTION_USB_ATTACHED:
                    findSerialPortDevice(); // A USB device has been attached. Try to open it as a Serial port
                    break;
                case ACTION_USB_DETACHED:
                    // Usb device was disconnected. send an intent to the Main Activity
                    Intent intent = new Intent(ACTION_USB_DISCONNECTED);
                    arg0.sendBroadcast(intent);
                    peripheryManager.removeIf(sensor -> sensor instanceof UsbSensor);
                    break;
            }
        }
    };

    /*
     * onCreate will be executed when service is started. It configures an IntentFilter to listen for
     * incoming Intents (USB ATTACHED, USB DETACHED...) and it tries to open a serial port.
     */
    @Override
    public void onCreate() {
        UsbService.SERVICE_CONNECTED = true;
        setFilter();
        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        findSerialPortDevice();
    }

    /* MUST READ about services
     * http://developer.android.com/guide/components/services.html
     * http://developer.android.com/guide/components/bound-services.html
     */
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(usbReceiver);
        UsbService.SERVICE_CONNECTED = false;
    }

    private void findSerialPortDevice() {
        // This snippet will try to open the first encountered usb device connected, excluding usb root hubs
        HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
        if (!usbDevices.isEmpty()) {

            UsbDevice device = null;
            // first, dump the map for diagnostic purposes
            for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
                device = entry.getValue();
                Log.d(TAG, String.format("USBDevice.HashMap (vid:pid) (%X:%X)-%b class:%X:%X name:%s",
                        device.getVendorId(), device.getProductId(),
                        UsbSerialDevice.isSupported(device),
                        device.getDeviceClass(), device.getDeviceSubclass(),
                        device.getDeviceName()));
            }

            for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
                device = entry.getValue();
                int deviceVID = device.getVendorId();
                int devicePID = device.getProductId();

//                if (deviceVID != 0x1d6b && (devicePID != 0x0001 && devicePID != 0x0002 && devicePID != 0x0003)) {
                if (UsbSerialDevice.isSupported(device)) {
                    // There is a device connected to our Android device. Try to open it as a Serial Port.
                    requestUserPermission(device);
                    break;
                } else {
                    device = null;
                }
            }
            if (device == null) {
                // There is no USB devices connected (but usb host were listed). Send an intent to MainActivity.
                Intent intent = new Intent(ACTION_NO_USB);
                sendBroadcast(intent);
            }
        } else {
            Log.d(TAG, "findSerialPortDevice() usbManager returned empty device list.");
            // There is no USB devices connected. Send an intent to MainActivity
            Intent intent = new Intent(ACTION_NO_USB);
            sendBroadcast(intent);
        }
    }

    private void setFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(ACTION_USB_DETACHED);
        filter.addAction(ACTION_USB_ATTACHED);
        registerReceiver(usbReceiver, filter);
    }

    /*
     * Request user permission. The response will be received in the BroadcastReceiver
     */
    private void requestUserPermission(UsbDevice device) {
        Log.d(TAG, String.format("requestUserPermission(%X:%X)", device.getVendorId(), device.getProductId()));
        Intent intent = new Intent(ACTION_USB_PERMISSION);
        intent.putExtra(USB_DEVICE_EXTRA_KEY, device);
        PendingIntent mPendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        usbManager.requestPermission(device, mPendingIntent);
    }

    public class UsbBinder extends Binder {
        public UsbService getService() {
            return UsbService.this;
        }
    }

    public static int[] readPacket(byte[] bytesIn){
       if (bytesIn.length < 26) {
           return null;
       }
       if (bytesIn[1] == 0){
           return null;
       }
       int start = 2;

       int[] result = new int[9];
       //8 sensors
       for (int i = 0; i < 8; i++) {
           int offset = start + i * 3;
           result[i] = interpret24bitAsInt32(bytesIn, offset);
       }
       //appending packet number
       result[8] = (int) bytesIn[1];
       return result;
    }

    static int interpret24bitAsInt32(byte[] byteArray, int offset) {
        //little endian
        int newInt = (
                ((0xFF & byteArray[offset]) << 16) |
                        ((0xFF & byteArray[offset + 1]) << 8) |
                        (0xFF & byteArray[offset + 2])
        );
        if ((newInt & 0x00800000) > 0) {
            newInt |= 0xFF000000;
        } else {
            newInt &= 0x00FFFFFF;
        }
        return newInt;
    }
}
