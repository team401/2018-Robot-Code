package org.team401.robot2018

import org.team401.robot2018.auto.motion.PDVA

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
        const val DRIVE_LEFT_REAR_CAN = 0
        const val DRIVE_LEFT_MIDR_CAN = 1
        const val DRIVE_LEFT_MIDF_CAN = 2
        const val DRIVE_LEFT_FRONT_CAN = 3
        const val DRIVE_RIGHT_FRONT_CAN = 11
        const val DRIVE_RIGHT_MIDF_CAN = 12
        const val DRIVE_RIGHT_MIDR_CAN = 13
        const val DRIVE_RIGHT_REAR_CAN = 14

        const val ELEVATOR_MASTER_CAN = 7
        const val ELEVATOR_SLAVE_1_CAN = 10
        const val ELEVATOR_SLAVE_2_CAN = 9
        const val ELEVATOR_SLAVE_3_CAN = 8

        const val INTAKE_LEFT_CAN = 5
        const val INTAKE_RIGHT_CAN = 6
        const val INTAKE_FOLDING_CAN = 4
    }

    object PDPChannels {
        const val DRIVE_LEFT_REAR_PDP = 15
        const val DRIVE_LEFT_MIDR_PDP = 14
        const val DRIVE_LEFT_MIDF_PDP = 13
        const val DRIVE_LEFT_FRONT_PDP = 12
        const val DRIVE_RIGHT_FRONT_PDP = 3
        const val DRIVE_RIGHT_MIDF_PDP = 2
        const val DRIVE_RIGHT_MIDR_PDP = 1
        const val DRIVE_RIGHT_REAR_PDP = 0

        const val ELEVATOR_MASTER_PDP = 7
        const val ELEVATOR_SLAVE_1_PDP = 6
        const val ELEVATOR_SLAVE_2_PDP = 5
        const val ELEVATOR_SLAVE_3_PDP = 4

        const val INTAKE_LEFT_PDP = 5
        const val INTAKE_RIGHT_PDP = 6
        const val INTAKE_FOLDING_PDP = 4
    }

    object Pneumatics {
        const val SHIFTER_SOLENOID = 2

        const val ELEVATOR_SHIFTER_SOLENOID = 3
        const val ELEVATOR_DEPLOY_SOLENOID = 0
        const val ELEVATOR_RATCHET_SOLENOID = 4
        const val ELEVATOR_KICKER_SOLENOID = 6
        const val ELEVATOR_CLAMP_SOLENOID = 5

        const val RUNGS_DEPLOY_SOLENOID = 0
    }

    object DrivetrainParameters {
        const val DELTA = 0.5 //fixme (testme)
        const val SPEED_THRESHOLD = 7.5 //fixme (testme)
        const val SPEED_SPLIT = 3.5 //fixme (testme)
        //above should be some value between the low gear speed and the high gear speed

        const val WHEEL_RADIUS = 2.0 //in
        const val WHEELBASE = 0.0 //in

        const val INVERT_LEFT = true
        const val INVERT_RIGHT = false
        const val INVERT_SHIFTER = true

        const val CURRENT_LIMIT_CONTINUOUS_HIGH = 30 //A
        const val CURRENT_LIMIT_PEAK_HIGH = 40 //A
        const val CURRENT_LIMIT_TIMEOUT_HIGH = 100 //ms
        
        const val CURRENT_LIMIT_CONTINUOUS_LOW = 30 //A
        const val CURRENT_LIMIT_PEAK_LOW = 40 //A
        const val CURRENT_LIMIT_TIMEOUT_LOW = 100 //ms

        const val DOWNSHIFT_CURRENT = 30 //A

        const val CLOSED_LOOP_RAMP = 0.0
        const val OPEN_LOOP_RAMP = .25

        val LEFT_PDVA = PDVA()
        val RIGHT_PDVA = PDVA()
    }

    object MotionProfileParameters {
        const val TICKS_PER_REV = 4096.0

        const val SLOT_IDX = 0
        const val PID_IDX = 0

        const val TIMEOUT = 10

        const val BASE_PERIOD = 0

        const val NEUTRAL_DEADBAND = 0.01

        const val MIN_POINTS = 10

    }

    object ElevatorParameters {
        const val DEPLOY_TIMER = 5000L //ms

        const val HOMING_RATE = -.1 //percent vbus

        const val MANUAL_RATE = 4096/4.0 //ticks / 20 ms

        const val CURRENT_LIMIT_CONTINUOUS = 30 //A

        const val MAX_POS = 40960.0 //ticks

        const val HOME_POS = 0.0 //ticks
        const val CUBE_POS = HOME_POS + 0.0 //ticks
        const val SWITCH_POS = HOME_POS + 0.0 //ticks
        const val SCALE_POS = HOME_POS + 0.0 //ticks
        const val SCALE_POS_HIGH = SCALE_POS + 0.0 //ticks
        const val SCALE_POS_LOW = SCALE_POS - 0.0 //ticks

        const val PITCH_DIAMETER = 1.805 //in
    }

    object IntakeParameters {
        const val INTAKE_RATE = 1.0
        const val REVERSE_RATE = -0.7

        const val STOWED_POS = 650.0
        const val INTAKE_POS = 2400.0
        const val GRAB_POS = (STOWED_POS + INTAKE_POS)/2.0

        const val HAVE_CUBE_CURRENT = 0.0
        const val VOLTAGE_LIMIT = 1.0 //Percent vbus

        const val INVERT_LEFT = true
        const val INVERT_RIGHT = true

        const val INTAKE_VOLTAGE = 12.0

        const val FOLDING_PEAK_LIMIT = 30
        const val FOLDING_CONTINUOUS_LIMIT = 10
        const val FOLDING_PEAK_LIMIT_DUR = 100

        const val FOLDING_PEAK_OUTPUT_FORWARD = 0.5
        const val FOLDING_PEAK_OUTPUT_REVERSE = -0.5

        const val LEFT_PEAK_LIMIT = 40
        const val RIGHT_PEAK_LIMIT = 40

        const val LEFT_CONTINUOUS_LIMIT = 30
        const val RIGHT_CONTINUOUS_LIMIT = 30

        const val LEFT_PEAK_LIMIT_DUR = 100
        const val RIGHT_PEAK_LIMIT_DUR = 100



        object PIDF {
            const val P = 3.5
            const val I = 0.0
            const val D = 35.0
            const val F = 0.0
        }
    }

    object RungsParameters {
        const val DEPLOY_TIMER = 5000L //ms
    }

    object MJPEGParameters {
        const val ADDRESS = "10.4.1.3"
        const val PORT = "1180"
    }

    object ReportingParameters {
        const val REPORTING_RATE = 100L //ms
    }
}