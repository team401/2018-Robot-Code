package org.team401.robot2018

import org.snakeskin.dsl.HumanControls
import org.snakeskin.dsl.machine
import org.snakeskin.logic.scalars.SquareScalar
import org.team401.robot2018.subsystems.*

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
    /*
    whenButton(Buttons.TRIGGER) {
        pressed {
            DrivetrainSubsystem.machine(DRIVE_MACHINE).setState("testAccel")
        }

        released {
            DrivetrainSubsystem.machine(DRIVE_MACHINE).setState(DriveStates.OPEN_LOOP)
        }
    }
    */
}

val RightStick = HumanControls.t16000m(1) {
//test masher code
    /*
    whenButton(Buttons.TRIGGER){
        pressed {
            IntakeSubsystem.machine(INTAKE_WHEELS_MACHINE).setState(IntakeWheelsStates.INTAKE)
            IntakeSubsystem.machine(INTAKE_FOLDING_MACHINE).setState(IntakeFoldingStates.INTAKE)
        }
        released {
            IntakeSubsystem.machine(INTAKE_WHEELS_MACHINE).setState(IntakeWheelsStates.IDLE)
            IntakeSubsystem.machine(INTAKE_FOLDING_MACHINE).setState(IntakeFoldingStates.GRAB)
        }
    }
    whenButton(Buttons.STICK_BOTTOM){
        pressed {
            IntakeSubsystem.machine(INTAKE_WHEELS_MACHINE).setState(IntakeWheelsStates.REVERSE)
            IntakeSubsystem.machine(INTAKE_FOLDING_MACHINE).setState(IntakeFoldingStates.INTAKE)
        }
        released {
            IntakeSubsystem.machine(INTAKE_WHEELS_MACHINE).setState(IntakeWheelsStates.IDLE)
            IntakeSubsystem.machine(INTAKE_FOLDING_MACHINE).setState(IntakeFoldingStates.GRAB)
        }
    }
    whenButton(Buttons.STICK_LEFT){
        pressed {
            IntakeSubsystem.machine(INTAKE_FOLDING_MACHINE).setState(IntakeFoldingStates.STOWED)
        }
    }
    whenButton(Buttons.STICK_RIGHT){
        pressed {
            IntakeSubsystem.machine(INTAKE_FOLDING_MACHINE).setState(IntakeFoldingStates.GRAB)
        }
    }
    */

}
//val MasherBox = HumanControls.saitekButtonBox(2){}