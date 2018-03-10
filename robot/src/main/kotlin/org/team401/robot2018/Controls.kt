package org.team401.robot2018

import edu.wpi.first.wpilibj.DriverStation
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
    val elevatorDeployMachine = ElevatorSubsystem.machine(ELEVATOR_DEPLOY_MACHINE)
    val elevatorRatchetMachine = ElevatorSubsystem.machine(ELEVATOR_RATCHET_MACHINE)

    //Elevator setpoints
    whenHatChanged(Hats.D_PAD) {
        when (it) {
            Direction.CENTER -> {}

            Direction.NORTH -> elevatorMachine.setState(ElevatorStates.POS_SCALE_HIGH)
            Direction.EAST -> elevatorMachine.setState(ElevatorStates.POS_SWITCH)
            Direction.SOUTH -> elevatorMachine.setState(ElevatorStates.POS_DRIVE)
            Direction.WEST -> elevatorMachine.setState(ElevatorStates.POS_SCALE)

           // Direction.NORTH -> elevatorMachine.setState(ElevatorStates.POS_DRIVE)
           // Direction.SOUTH -> elevatorMachine.setState(ElevatorStates.POS_SWITCH)
        }
    }

    //Intake control
    whenButton(Buttons.A) {
        pressed {
            elevatorMachine.setState(ElevatorStates.GO_TO_COLLECTION)
            Thread.sleep(100)
            elevatorClampMachine.setState(ElevatorClampStates.UNCLAMPED)
            intakeFolding.setState(IntakeFoldingStates.GO_TO_INTAKE)
            intakeWheels.setState(IntakeWheelsStates.REVERSE)
        }
        released {
            intakeWheels.setState(IntakeWheelsStates.IDLE)
            intakeFolding.setState(IntakeFoldingStates.STOWED)
        }
    }

    whenButton(Buttons.B) {
        pressed {
            elevatorClampMachine.setState(ElevatorClampStates.UNCLAMPED)
            intakeFolding.setState(IntakeFoldingStates.GRAB)
            elevatorMachine.setState(ElevatorStates.GO_TO_COLLECTION)
            Thread.sleep(100)
            intakeFolding.setState(IntakeFoldingStates.GO_TO_INTAKE)
            intakeWheels.setState(IntakeWheelsStates.INTAKE)
        }
        released {
            intakeWheels.setState(IntakeWheelsStates.IDLE)
            intakeFolding.setState(IntakeFoldingStates.STOWED)
        }
    }

    whenButton(Buttons.Y) {
        pressed {
            elevatorClampMachine.setState(ElevatorClampStates.UNCLAMPED)
            intakeWheels.setState(IntakeWheelsStates.IDLE)
            intakeFolding.setState(IntakeFoldingStates.GRAB)
        }
    }

    whenButton(Buttons.X) {
        pressed {
            elevatorClampMachine.setState(ElevatorClampStates.UNCLAMPED)
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
            elevatorClampMachine.toggle(ElevatorClampStates.CLAMPED, ElevatorClampStates.UNCLAMPED)
        }
    }

    //Climbing and rungs
    whenButton(Buttons.BACK) {
        pressed {
            rungsMachine.setState(RungsStates.DEPLOY)
            elevatorShifterMachine.setState(ElevatorShifterStates.LOW)
            elevatorMachine.setState(ElevatorStates.CLIMB_MANUAL)
        }
    }

    whenButton(Buttons.START) {
        pressed {
            elevatorRatchetMachine.setState(ElevatorRatchetStates.LOCKED)
            elevatorMachine.setState(ElevatorStates.CLIMB)
        }
    }
}