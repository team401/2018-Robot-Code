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
class PracticeConstants: ConstantsBase() {
    override val DrivetrainParameters = object: DrivetrainParametersConfig() {
        override val DELTA = 0.5
        override val SPEED_THRESHOLD = 7.5
        override val SPEED_SPLIT = 3.5

        override val LEFT_PDVA = PDVA(1/19.0, 0/3.0, 1/1200.0, 0.0)
        override val RIGHT_PDVA = PDVA(1/19.0, 0/3.0, 1/1200.0, 0.0)

        override val HEADING_GAIN = .0025

        override val TIP_CORRECTION_SCALAR = 10
        override val PITCH_CORRECTION_MIN = 10
        override val ROLL_CORRECTION_MIN = 10
    }

    override val ElevatorParameters = object: ElevatorParametersConfig() {
        override val RATCHET_UNLOCKED_SERVO_POS = 84.0
        override val RATCHET_LOCKED_SERVO_POS = 97.0

        override val PIDF = object: PIDF {
            override val P = 0.5
            override val I = 0.0
            override val D = 0.0
            override val F = 1/100.0
        }
    }

    override val IntakeParameters = object: IntakeParametersConfig() {
        override val STOWED_POS = 780.0
        override val INTAKE_POS = 2500.0
        override val PIDF = object: PIDF {
            override val P = 3.5
            override val I = 0.0
            override val D = 35.0
            override val F = 0.0
        }
    }
}