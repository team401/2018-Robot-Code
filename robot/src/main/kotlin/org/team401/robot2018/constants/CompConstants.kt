package org.team401.robot2018.constants

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

        override val LEFT_PDVA = PDVA(1/19.0, 0/3.0, 1/1100.0, 0.0)
        override val RIGHT_PDVA = PDVA(1/19.0, 0/3.0, 1/1100.0, 0.0)

        override val HEADING_GAIN = .01

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
        override val STOWED_POS = 790.0
        override val INTAKE_POS = 2800.0
        override val GRAB_POS = (STOWED_POS + INTAKE_POS) / 2.0
        override val PIDF = object: PIDF {
            override val P = 4.0
            override val I = 0.0
            override val D = 35.0
            override val F = 0.0
        }
    }
}