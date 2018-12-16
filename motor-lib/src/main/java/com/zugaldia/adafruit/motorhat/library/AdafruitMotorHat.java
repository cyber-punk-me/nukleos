package com.zugaldia.adafruit.motorhat.library;

/**
 * A port of `Adafruit_MotorHAT` to Android Things.
 * <p>
 * https://github.com/adafruit/Adafruit-Motor-HAT-Python-Library/blob/master/Adafruit_MotorHAT/Adafruit_MotorHAT.py
 */

public class AdafruitMotorHat {

    public static final int MOTOR_FREQUENCY = 1600;

    public static final int FORWARD = 1;
    public static final int BACKWARD = 2;
    public static final int BRAKE = 3;
    public static final int RELEASE = 4;

    public static final int SINGLE = 1;
    public static final int DOUBLE = 2;
    public static final int INTERLEAVE = 3;
    public static final int MICROSTEP = 4;

    // Stepper motor speed seems to be pretty slow with the Adafruit MotorHat,
    // so for maximum speed, you can turn off all delays between steps by
    // setting SLEEP_BETWEEN_STEPS to false.
    public static final boolean SLEEP_BETWEEN_STEPS = true;

    private AdafruitPwm pwm;
    private AdafruitDCMotor[] motors;
    private AdafruitStepperMotor[] steppers;

    public AdafruitMotorHat() {

        // TODO: Pass in address so we can stack them.  Not scalable now.
        pwm = new AdafruitPwm();

        pwm.setPWMFreq(MOTOR_FREQUENCY);

        motors = new AdafruitDCMotor[]{
                new AdafruitDCMotor(this, 0),
                new AdafruitDCMotor(this, 1),
                new AdafruitDCMotor(this, 2),
                new AdafruitDCMotor(this, 3)
        };

        steppers = new AdafruitStepperMotor[]{
                // Might want to parameterize steps per motor (200 here)
                new AdafruitStepperMotor(this, 1, 200, SLEEP_BETWEEN_STEPS),
                new AdafruitStepperMotor(this, 2, 200, SLEEP_BETWEEN_STEPS)
        };
    }

    public AdafruitPwm getPwm() {
        return pwm;
    }

    public void setPin(int pin, int value) {
        if ((pin < 0) || (pin > 15)) {
            throw new RuntimeException("PWM pin must be between 0 and 15 inclusive");
        }
        if ((value != 0) && (value != 1)) {
            throw new RuntimeException("Pin value must be 0 or 1!");
        }
        if ((value == 0)) {
            pwm.setPWM(pin, 0, 4096);
        }
        if ((value == 1)) {
            pwm.setPWM(pin, 4096, 0);
        }
    }

    public AdafruitDCMotor getMotor(int num) {
        if ((num < 1) || (num > 4)) {
            throw new RuntimeException("MotorHAT Motor must be between 1 and 4 inclusive");
        }
        return motors[num - 1];
    }

    public AdafruitStepperMotor getStepper(int num) {
        if ((num < 1) || (num > 2)) {
            throw new RuntimeException("MotorHAT Stepper must be between 1 and 2 inclusive");
        }
        return steppers[num - 1];
    }

    public void close() {
        pwm.close();
    }
}
