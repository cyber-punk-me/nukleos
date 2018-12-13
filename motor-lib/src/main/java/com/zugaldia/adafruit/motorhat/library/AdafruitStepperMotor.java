package com.zugaldia.adafruit.motorhat.library;

import android.util.Log;

/**
 * A port of `Adafruit_StepperMotor` to Android Things.
 * <p>
 * https://github.com/adafruit/Adafruit-Motor-HAT-Python-Library/blob/master/Adafruit_MotorHAT/Adafruit_StepperMotor.py
 */

public class AdafruitStepperMotor {

    // Translated from Python Adafruit_StepperMotor by Jerry Destremps, July 2017

    // A sinusoidal curve -- NOT LINEAR! (use the following with 16 steps)
    // private static int[] __MICROSTEPCURVE = {0, 25, 50, 74, 98, 120, 141, 162, 180, 197, 212, 225, 236, 244, 250, 253, 255};

    private static int[] MICROSTEP_CURVE = {0, 50, 98, 142, 180, 212, 236, 250, 255};
    private int MICROSTEPS = 8;         // 8 or 16
    private AdafruitMotorHat MC;
    private int revsteps;
    private double sec_per_step;
    private int currentstep;
    private boolean sleepBetweenSteps = true;
    private boolean alreadySetPWM = false;

    private int PWMA = 8;
    private int AIN2 = 9;
    private int AIN1 = 10;
    private int PWMB = 13;
    private int BIN2 = 12;
    private int BIN1 = 11;

    public AdafruitStepperMotor(AdafruitMotorHat MC, int motorNumber, int steps, boolean sleepBetweenSteps) {

        this.MC = MC;
        this.revsteps = steps;
        this.sec_per_step = 0.01;
        this.currentstep = 0;
        this.sleepBetweenSteps = sleepBetweenSteps;

        // Really retarded handling of zero based motor numbers but whatever for now.
        motorNumber -= 1;

        if (motorNumber == 0) {

            this.PWMA = 8;
            this.AIN2 = 9;
            this.AIN1 = 10;
            this.PWMB = 13;
            this.BIN2 = 12;
            this.BIN1 = 11;

        } else if (motorNumber == 1) {

            this.PWMA = 2;
            this.AIN2 = 3;
            this.AIN1 = 4;
            this.PWMB = 7;
            this.BIN2 = 6;
            this.BIN1 = 5;

        } else {
            Log.e(".", "MotorHAT Stepper must be between 1 and 2 inclusive");
        }
    }

    // If you just set speed, it's assumed you want to sleep between steps (which is what setting speed affects)
    // so the overloaded method is called with sleepBetweenSteps set to true.
    public void setSpeed(int rpm) {
        setSpeed(rpm, true);
    }

    public void setSpeed(int rpm, boolean sleepBetweenSteps) {
        this.sleepBetweenSteps = sleepBetweenSteps;
        sec_per_step = 60.0 / (revsteps * rpm);
    }

    // Leaving as direct translation from Python for now.  Want visual parity, not optimizing.
    private int oneStep(int dir, int style) {

        int pwm_a = 255;
        int pwm_b = 255;

        // Set up coil energizing!
        int coils[] = {0, 0, 0, 0};

        int step2coils[][] = {
                {1, 0, 0, 0},
                {1, 1, 0, 0},
                {0, 1, 0, 0},
                {0, 1, 1, 0},
                {0, 0, 1, 0},
                {0, 0, 1, 1},
                {0, 0, 0, 1},
                {1, 0, 0, 1}
        };

        // First determine what sort of stepping procedure we're up to
        if (style == AdafruitMotorHat.SINGLE) {

            if ((currentstep / (MICROSTEPS / 2)) % 2 > 0) {

                // We're at an odd step, weird
                if (dir == AdafruitMotorHat.FORWARD) {

                    // Probably don't need parens, but being extra careful.  Don't want to fry boards.
                    currentstep += (MICROSTEPS / 2);
                } else {
                    currentstep -= (MICROSTEPS / 2);
                }

            } else {

                // Go to next even step
                if (dir == AdafruitMotorHat.FORWARD) {
                    currentstep += MICROSTEPS;
                } else {
                    currentstep -= MICROSTEPS;
                }
            }

        } else if (style == AdafruitMotorHat.DOUBLE) {

            //Log.d(".", "Double step");

            if ((currentstep / (MICROSTEPS / 2)) % 2 == 0) {

                // We're at an even step, weird
                if (dir == AdafruitMotorHat.FORWARD) {
                    currentstep += (MICROSTEPS / 2);
                } else {
                    currentstep -= (MICROSTEPS / 2);
                }

            } else {

                // Go to next even step
                if (dir == AdafruitMotorHat.FORWARD) {
                    currentstep += MICROSTEPS;
                } else {
                    currentstep -= MICROSTEPS;
                }
            }

        } else if (style == AdafruitMotorHat.INTERLEAVE) {

            if (dir == AdafruitMotorHat.FORWARD) {
                currentstep += (MICROSTEPS / 2);
            } else {
                currentstep -= (MICROSTEPS / 2);
            }

        } else if (style == AdafruitMotorHat.MICROSTEP) {

            if (dir == AdafruitMotorHat.FORWARD) {
                currentstep++;
            } else {

                currentstep--;
                // Go to next 'step' and wrap around
                currentstep += (MICROSTEPS * 4);
                currentstep %= (MICROSTEPS * 4);
            }

            pwm_a = 0;
            pwm_b = 0;

            if (currentstep >= 0 && currentstep < MICROSTEPS) {
                pwm_a = MICROSTEP_CURVE[MICROSTEPS - currentstep];
                pwm_b = MICROSTEP_CURVE[currentstep];
            } else if (currentstep >= MICROSTEPS && currentstep < MICROSTEPS * 2) {
                pwm_a = MICROSTEP_CURVE[currentstep - MICROSTEPS];
                pwm_b = MICROSTEP_CURVE[MICROSTEPS * 2 - currentstep];
            } else if (currentstep >= MICROSTEPS * 2 && currentstep < MICROSTEPS * 3) {
                pwm_a = MICROSTEP_CURVE[MICROSTEPS * 3 - currentstep];
                pwm_b = MICROSTEP_CURVE[currentstep - MICROSTEPS * 2];
            } else if (currentstep >= MICROSTEPS * 3 && currentstep < MICROSTEPS * 4) {
                pwm_a = MICROSTEP_CURVE[currentstep - MICROSTEPS * 3];
                pwm_b = MICROSTEP_CURVE[MICROSTEPS * 4 - currentstep];
            }
        }

        // Go to next 'step' and wrap around
        currentstep += (MICROSTEPS * 4);
        currentstep %= (MICROSTEPS * 4);

        // Only really used for microstepping, otherwise always on!
        if (!alreadySetPWM || style == AdafruitMotorHat.MICROSTEP) {

            // If we're not doing microstepping, then the value will always be the same,
            // so only do it once.  Otherwise it wastes 5 milliseconds according to logs.
            alreadySetPWM = true;
            MC.getPwm().setPWM(PWMA, 0, pwm_a * 16);
            MC.getPwm().setPWM(PWMB, 0, pwm_b * 16);
        }

        if (style == AdafruitMotorHat.MICROSTEP) {
            if (currentstep >= 0 && currentstep < MICROSTEPS) {
                coils = new int[]{1, 1, 0, 0};
            } else if (currentstep >= MICROSTEPS && currentstep < MICROSTEPS * 2) {
                coils = new int[]{0, 1, 1, 0};
            } else if (currentstep >= MICROSTEPS * 2 && currentstep < MICROSTEPS * 3) {
                coils = new int[]{0, 0, 1, 1};
            } else if (currentstep >= MICROSTEPS * 3 && currentstep < MICROSTEPS * 4) {
                coils = new int[]{1, 0, 0, 1};
            }
        } else {
            coils = step2coils[currentstep / (MICROSTEPS / 2)];
        }

        //Log.d(".", "coils state = " + coils);

        MC.setPin(AIN2, coils[0]);
        MC.setPin(BIN1, coils[1]);
        MC.setPin(AIN1, coils[2]);
        MC.setPin(BIN2, coils[3]);

        return currentstep;
    }

    public void step(int steps, int direction, int stepstyle) {

        double s_per_s = sec_per_step;
        int lateststep = 0;

        if (stepstyle == AdafruitMotorHat.INTERLEAVE) {
            s_per_s /= 2.0;
        } else if (stepstyle == AdafruitMotorHat.MICROSTEP) {
            s_per_s /= MICROSTEPS;
            steps *= MICROSTEPS;
        }

        Log.d(".", "secs per step: " + s_per_s);

        for (int s = 1; s <= steps; s++) {

            lateststep = oneStep(direction, stepstyle);

            if (sleepBetweenSteps) {
                try {
                    Thread.sleep((long) s_per_s * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        if (stepstyle == AdafruitMotorHat.MICROSTEP) {

            // This is an edge case, if we are in between full steps, lets just keep going
            // So we end on a full step
            while (lateststep != 0 && lateststep != MICROSTEPS) {

                lateststep = oneStep(direction, stepstyle);

                if (sleepBetweenSteps) {
                    try {
                        Thread.sleep((long) s_per_s * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
