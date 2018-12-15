package com.nilhcem.blefun.common;

public interface MotorsInt {

    int MOTORS_COUNT = 4;

    byte FORWARD = 1;
    byte BACKWARD = 2;
    byte BRAKE = 3;
    byte RELEASE = 4;

    byte SINGLE = 1;
    byte DOUBLE = 2;
    byte INTERLEAVE = 3;
    byte MICROSTEP = 4;

    void connect(Object context);

    void spinMotor(byte iMotor, byte direction, byte speed);

    void stopMotors();
    
}
