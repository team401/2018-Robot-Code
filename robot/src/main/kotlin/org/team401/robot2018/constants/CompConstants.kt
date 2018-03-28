package org.team401.robot2018.constants

import org.team401.robot2018.auto.motion.DriveGains
import org.team401.robot2018.auto.motion.PDVA

/*
 * 2018-Robot-Code - Created on 3/7/18
 * Author: Cameron Earle
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at project root
 */

/**
 * @author Cameron Earle
 * @version 3/7/18
 */
class CompConstants: ConstantsBase() {
    override val DrivetrainParameters = object: DrivetrainParametersConfig() {
        override val DELTA = 0.5
        override val SPEED_THRESHOLD = 7.5
        override val SPEED_SPLIT = 3.5

        override val LEFT_PDVA = PDVA(.35, 0.0, 1/650.0, 0.0005)
        override val RIGHT_PDVA = PDVA(.35, 0.0, 1/650.0, 0.0005)
        override val DRIVE_GAINS = DriveGains(0.4, 0.002, 1/500.0, .0005, .023)

        override val HEADING_GAIN = .017
        override val HEADING_D = .5

        override val TIP_CORRECTION_SCALAR = 10
        override val PITCH_CORRECTION_MIN = 10
        override val ROLL_CORRECTION_MIN = 10
    }

    override val ElevatorParameters = object: ElevatorParametersConfig() {
        override val RATCHET_UNLOCKED_SERVO_POS = 74.0
        override val RATCHET_LOCKED_SERVO_POS = 90.0

        override val PIDF = object: PIDF {
            override val P = 0.5
            override val I = 0.0
            override val D = 0.0
            override val F = 1/100.0
        }
    }

    override val IntakeParameters = object: IntakeParametersConfig() {
        override val HAVE_CUBE_CURRENT_CLAMP = 10.0
        override val HAVE_CUBE_CURRENT_LEFT_HOLD = 0.3
        override val HAVE_CUBE_CURRENT_LEFT_INTAKE = 4.5
        override val HAVE_CUBE_CURRENT_RIGHT_HOLD = 0.3
        override val HAVE_CUBE_CURRENT_RIGHT_INTAKE = 4.5

        override val PIDF = object: PIDF {
            override val P = 4.0
            override val I = 0.0
            override val D = 35.0
            override val F = 0.0
        }
    }
}