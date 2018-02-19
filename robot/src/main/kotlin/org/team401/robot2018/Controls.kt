package org.team401.robot2018

import org.snakeskin.dsl.HumanControls
import org.snakeskin.dsl.machine
import org.snakeskin.logic.Direction
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
    val driveShiftMachine = DrivetrainSubsystem.machine(DRIVE_SHIFT_MACHINE)

    invertAxis(Axes.PITCH)
    whenButton(Buttons.TRIGGER) {
        pressed {
            driveShiftMachine.setState(DriveShiftStates.LOW)
        }
        released {
            driveShiftMachine.setState(DriveShiftStates.HIGH)
        }
    }
}

val RightStick = HumanControls.t16000m(1) {

}

val Gamepad = HumanControls.f310(2) {
    val elevatorMachine = ElevatorSubsystem.machine(ELEVATOR_MACHINE)
    val elevatorClampMachine = ElevatorSubsystem.machine(ELEVATOR_CLAMP_MACHINE)
    val elevatorKickerMachine = ElevatorSubsystem.machine(ELEVATOR_KICKER_MACHINE)
    val intakeFolding = IntakeSubsystem.machine(INTAKE_FOLDING_MACHINE)
    val intakeWheels = IntakeSubsystem.machine(INTAKE_WHEELS_MACHINE)
    val rungsMachine = RungsSubsystem.machine(RUNGS_MACHINE)
    val elevatorShifterMachine = ElevatorSubsystem.machine(ELEVATOR_SHIFTER_MACHINE)

    //Elevator setpoints
    whenHatChanged(Hats.D_PAD) {
        when (it) {
            Direction.CENTER -> {}
            /*
            Direction.NORTH -> elevatorMachine.setState(ElevatorStates.POS_SCALE_HIGH)
            Direction.EAST -> elevatorMachine.setState(ElevatorStates.POS_SWITCH)
            Direction.SOUTH -> elevatorMachine.setState(ElevatorStates.POS_DRIVE)
            Direction.WEST -> elevatorMachine.setState(ElevatorStates.POS_SCALE_LOW)
            */
            Direction.NORTH -> elevatorMachine.setState(ElevatorStates.POS_DRIVE)
            Direction.SOUTH -> elevatorMachine.setState(ElevatorStates.POS_SWITCH)
        }
    }

    //Intake control
    whenButton(Buttons.A) {
        pressed {
            intakeFolding.setState(IntakeFoldingStates.INTAKE)
            intakeWheels.setState(IntakeWheelsStates.REVERSE)
        }
        released {
            intakeWheels.setState(IntakeWheelsStates.IDLE)
            intakeFolding.setState(IntakeFoldingStates.STOWED)
        }
    }

    whenButton(Buttons.B) {
        pressed {
            intakeFolding.setState(IntakeFoldingStates.INTAKE)
            intakeWheels.setState(IntakeWheelsStates.INTAKE)
        }
        released {
            intakeWheels.setState(IntakeWheelsStates.IDLE)
            intakeFolding.setState(IntakeFoldingStates.STOWED)
        }
    }

    whenButton(Buttons.Y) {
        pressed {
            intakeWheels.setState(IntakeWheelsStates.IDLE)
            intakeFolding.setState(IntakeFoldingStates.GRAB)
        }
    }

    whenButton(Buttons.X) {
        pressed {
            intakeWheels.setState(IntakeWheelsStates.IDLE)
            intakeFolding.setState(IntakeFoldingStates.STOWED)
        }
    }

    //Elevator manual adjustment
    whenButton(Buttons.LEFT_STICK) {
        pressed {
            elevatorMachine.setState(ElevatorStates.MANUAL_ADJUSTMENT)
        }
    }

    //Scoring
    whenButton(Buttons.RIGHT_BUMPER) {
        pressed {
            elevatorClampMachine.setState(ElevatorClampStates.UNCLAMPED)
            elevatorKickerMachine.setState(ElevatorKickerStates.KICK)
        }

        released {
            elevatorKickerMachine.setState(ElevatorKickerStates.STOW)
        }
    }

    whenButton(Buttons.LEFT_BUMPER) {
        pressed {
            //elevatorMachine.setState(ElevatorStates.OPEN_LOOP_CONTROL)
            elevatorClampMachine.setState(ElevatorClampStates.CLAMPED)
        }

        released {
            elevatorClampMachine.setState(ElevatorClampStates.UNCLAMPED)
        }
    }

    /*
    //TODO FUTURE NOT NOW NO THANKS

    //Climbing and rungs
    whenButton(Buttons.BACK) {
        pressed {
            rungsMachine.setState(RungsStates.DEPLOY)
        }
    }

    whenButton(Buttons.START) {
        pressed {

        }
    }
    */
}