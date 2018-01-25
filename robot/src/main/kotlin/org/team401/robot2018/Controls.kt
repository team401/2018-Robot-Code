package org.team401.robot2018

import org.snakeskin.dsl.HumanControls
import org.snakeskin.dsl.machine
import org.snakeskin.logic.scalars.SquareScalar
import org.team401.robot2018.subsystems.DRIVE_MACHINE
import org.team401.robot2018.subsystems.DriveStates
import org.team401.robot2018.subsystems.DrivetrainSubsystem

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


val LeftStick = HumanControls.t16000m(0) {
    invertAxis(Axes.PITCH)
    whenButton(Buttons.TRIGGER) {
        pressed {
            DrivetrainSubsystem.machine(DRIVE_MACHINE).setState("testAccel")
        }

        released {
            DrivetrainSubsystem.machine(DRIVE_MACHINE).setState(DriveStates.OPEN_LOOP)
        }
    }
}

val RightStick = HumanControls.t16000m(1)
//HumanControls.saitekButtonBox(2)