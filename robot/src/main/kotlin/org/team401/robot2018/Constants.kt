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
    }

    object Pneumatics {
        const val SHIFTER_SOLENOID = 7
    }

    object DrivetrainParameters {
        const val WHEEL_RADIUS = 2.0
        const val WHEELBASE = 0.0

        const val INVERT_LEFT = false
        const val INVERT_RIGHT = false
        const val INVERT_SHIFTER = false

        const val CURRENT_LIMIT = 30

        const val CLOSED_LOOP_RAMP = .25
        const val OPEN_LOOP_RAMP = .25
    }
}