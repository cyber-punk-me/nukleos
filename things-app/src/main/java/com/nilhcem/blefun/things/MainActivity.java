package com.nilhcem.blefun.things;

import android.app.Activity;
import android.os.Bundle;

import com.nilhcem.blefun.common.Ints;
import com.zugaldia.adafruit.motorhat.library.AdafruitDCMotor;
import com.zugaldia.adafruit.motorhat.library.AdafruitMotorHat;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private final int MOTOR_COUNT = 4;
    private AwesomenessCounter mAwesomenessCounter;
    private final LuckyCat mLuckyCat = new LuckyCat();
    private final AdafruitMotorHat motorHat = new AdafruitMotorHat();
    private final GattServer mGattServer = new GattServer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAwesomenessCounter = new AwesomenessCounter(this);

        mLuckyCat.onCreate();
        mLuckyCat.updateCounter(mAwesomenessCounter.getCounterValue());

        mGattServer.onCreate(this, new GattServer.GattServerListener() {
            @Override
            public byte[] onReadRequest() {
                return Ints.toByteArray(mAwesomenessCounter.getCounterValue());
            }

            @Override
            public void onWriteRequest(byte[] value) {
                int count = mAwesomenessCounter.incrementCounterValue();
                if (count % 2 == 0) {
                    spinMotor((byte) 1, (byte) AdafruitMotorHat.FORWARD, (byte) 100);
                } else {
                    spinMotor((byte) 0, (byte) 0, (byte) 0);
                }
            }
        });
    }

    /**
     * @param iMotor
     * @param direction
     * @param speed
     */
    private void spinMotor(byte iMotor, byte direction, byte speed) {
        if (iMotor == 0 && direction == 0 && speed == 0) {
            stopMotors();
        } else {
            AdafruitDCMotor motor = motorHat.getMotor(iMotor);
            motor.setSpeed(speed);
            motor.run(direction);
        }
    }

    private void stopMotors() {
        for (int i = 1; i <= MOTOR_COUNT; i++) {
            motorHat.getMotor(i).run(AdafruitMotorHat.RELEASE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGattServer.onDestroy();
        mLuckyCat.onDestroy();
    }
}
