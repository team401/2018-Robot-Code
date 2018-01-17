package org.team401.robot2018

/*
 * 2018-Robot-Code - Created on 1/13/18
 * Author: Cameron Earle
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at project root
 */

/**
 * @author Cameron Earle
 * @version 1/13/18
 */
object Constants {
    object MotorControllers {
        const val DRIVE_LEFT_REAR_CAN = 1
        const val DRIVE_LEFT_MIDR_CAN = 2
        const val DRIVE_LEFT_MIDF_CAN = 3
        const val DRIVE_LEFT_FRONT_CAN = 4
        const val DRIVE_RIGHT_FRONT_CAN = 7
        const val DRIVE_RIGHT_MIDF_CAN = 8
        const val DRIVE_RIGHT_MIDR_CAN = 9
        const val DRIVE_RIGHT_REAR_CAN = 10

        const val ELEVATOR_MASTER_CAN = 0
        const val ELEVATOR_SLAVE_1_CAN = 0
        const val ELEVATOR_SLAVE_2_CAN = 0
        const val ELEVATOR_SLAVE_3_CAN = 0

        const val INTAKE_LEFT_PWM = 0
        const val INTAKE_RIGHT_PWM = 1
        const val INTAKE_FOLDING_CAN = 0
    }

    //PCM 0
    object Pneumatics {
        const val PCM_ID = 0

        const val SHIFTER_SOLENOID = 7

        const val ELEVATOR_SHIFTER_SOLENOID = 0
        const val ELEVATOR_DEPLOY_SOLENOID = 0
        const val ELEVATOR_RATCHET_SOLENOID = 0

        const val RUNGS_DEPLOY_SOLENOID = 0
    }

    object DrivetrainParameters {
        const val WHEEL_RADIUS = 2.0 //in
        const val WHEELBASE = 0.0 //in

        const val INVERT_LEFT = true
        const val INVERT_RIGHT = false
        const val INVERT_SHIFTER = false

        const val CURRENT_LIMIT_CONTINUOUS = 30 //A
        const val CURRENT_LIMIT_PEAK = 40 //A
        const val CURRENT_LIMIT_TIMEOUT = 100 //ms

        const val CLOSED_LOOP_RAMP = 0.0
        const val OPEN_LOOP_RAMP = .25
    }

    object MotionProfileParameters {
        const val TICKS_PER_REV = 4096.0

        const val SLOT_IDX = 0
        const val PID_IDX = 0

        const val TIMEOUT = 10

        const val BASE_PERIOD = 0

        const val NEUTRAL_DEADBAND = 0.01

        const val MIN_POINTS = 5

    }

    object ElevatorParameters {
        const val DEPLOY_TIMER = 5000L //ms

        const val HOMING_RATE = -.1

        const val MANUAL_RATE = 512.0

        const val CURRENT_LIMIT_CONTINUOUS = 30 //A
        const val CURRENT_LIMIT_PEAK = 40 //A
        const val CURRENT_LIMIT_TIMEOUT = 100 //ms
    }

    object IntakeParameters {
        const val INTAKE_RATE = .5
        const val REVERSE_RATE = -.5

        const val STOWED_POS = 0.0
        const val INTAKE_POS = 0.0
        const val GRAB_POS = 0.0
    }

    object RungsParameters {
        const val DEPLOY_TIMER = 5000L //ms
    }
}