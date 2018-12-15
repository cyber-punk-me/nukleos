package com.nilhcem.blefun.common;

public interface MotorsInt {

    int MOTORS_COUNT = 4;

    int FORWARD = 1;
    int BACKWARD = 2;
    int BRAKE = 3;
    int RELEASE = 4;

    int SINGLE = 1;
    int DOUBLE = 2;
    int INTERLEAVE = 3;
    int MICROSTEP = 4;

    void spinMotor(byte iMotor, byte direction, byte speed);

    void stopMotors();
    
}
