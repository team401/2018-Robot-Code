package org.team401.robot2018

import edu.wpi.first.wpilibj.DriverStation
import org.snakeskin.dsl.HumanControls
import org.snakeskin.dsl.machine
import org.snakeskin.logic.Direction
import org.team401.robot2018.etc.LED
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

    /*
    whenHatChanged(Hats.STICK_HAT) {
        when (it) {
            Direction.NORTH -> ElevatorSubsystem.machine(ELEVATOR_RATCHET_MACHINE).setState(ElevatorRatchetStates.LOCKED)
            Direction.CENTER -> ElevatorSubsystem.machine(ELEVATOR_RATCHET_MACHINE).setState(ElevatorRatchetStates.UNLOCKED)
        }
    }
    */

    //E STOP ELEVATOR
    //* . .
    //. . .
    whenButton(Buttons.STICK_BOTTOM) {
        pressed {
            ElevatorSubsystem.machine(ELEVATOR_MACHINE).setState(ElevatorStates.OPEN_LOOP_CONTROL)
            Elevator.estop = true
            DriverStation.reportWarning("ELEVATOR E-STOPPED!", false)
        }
    }

    //DEPLOY ELEVATOR
    //. . .
    //* . .
    whenButton(Buttons.STICK_LEFT) {
        pressed {
            ElevatorSubsystem.machine(ELEVATOR_DEPLOY_MACHINE).setState(ElevatorDeployStates.DEPLOY)
        }
    }

    //HOME ELEVATOR
    //. . .
    //. . *
    whenButton(Buttons.STICK_RIGHT) {
        pressed {
            ElevatorSubsystem.machine(ELEVATOR_MACHINE).setState(ElevatorStates.HOMING)
        }
    }
}

val RightStick = HumanControls.t16000m(1) {
    val elevatorMachine = ElevatorSubsystem.machine(ELEVATOR_MACHINE)
    val elevatorClampMachine = ElevatorSubsystem.machine(ELEVATOR_CLAMP_MACHINE)
    val elevatorKickerMachine = ElevatorSubsystem.machine(ELEVATOR_KICKER_MACHINE)
    val intakeFolding = IntakeSubsystem.machine(INTAKE_FOLDING_MACHINE)
    val intakeWheels = IntakeSubsystem.machine(INTAKE_WHEELS_MACHINE)
    val elevatorShifterMachine = ElevatorSubsystem.machine(ELEVATOR_SHIFTER_MACHINE)
    val elevatorDeployMachine = ElevatorSubsystem.machine(ELEVATOR_DEPLOY_MACHINE)
    val elevatorRatchetMachine = ElevatorSubsystem.machine(ELEVATOR_RATCHET_MACHINE)

    whenButton(Buttons.STICK_LEFT) {
        pressed {
            elevatorRatchetMachine.setState(ElevatorRatchetStates.UNLOCKED)
            elevatorShifterMachine.setState(ElevatorShifterStates.LOW)
            elevatorClampMachine.setState(ElevatorClampStates.CLAMPED)
            elevatorMachine.setState(ElevatorStates.START_CLIMB)
            LED.startClimb()
        }
    }

    whenButton(Buttons.STICK_RIGHT) {
        pressed {
            elevatorRatchetMachine.setState(ElevatorRatchetStates.LOCKED)
            elevatorMachine.setState(ElevatorStates.CLIMB)
        }

        released {
            elevatorMachine.setState(ElevatorStates.OPEN_LOOP_CONTROL)
            LED.finishClimb()
        }
    }

    whenButton(Buttons.STICK_BOTTOM) {
        pressed {
            elevatorRatchetMachine.setState(ElevatorRatchetStates.LOCKED)
            elevatorMachine.setState(ElevatorStates.CLIMB_HIGH)
        }

        released {
            elevatorMachine.setState(ElevatorStates.OPEN_LOOP_CONTROL)
            LED.finishClimb()
        }
    }
}

val Gamepad = HumanControls.f310(2) {
    val elevatorMachine = ElevatorSubsystem.machine(ELEVATOR_MACHINE)
    val elevatorClampMachine = ElevatorSubsystem.machine(ELEVATOR_CLAMP_MACHINE)
    val elevatorKickerMachine = ElevatorSubsystem.machine(ELEVATOR_KICKER_MACHINE)
    val intakeFolding = IntakeSubsystem.machine(INTAKE_FOLDING_MACHINE)
    val intakeWheels = IntakeSubsystem.machine(INTAKE_WHEELS_MACHINE)
    val elevatorShifterMachine = ElevatorSubsystem.machine(ELEVATOR_SHIFTER_MACHINE)
    val elevatorDeployMachine = ElevatorSubsystem.machine(ELEVATOR_DEPLOY_MACHINE)
    val elevatorRatchetMachine = ElevatorSubsystem.machine(ELEVATOR_RATCHET_MACHINE)

    invertAxis(Axes.LEFT_Y)

    //Elevator setpoints
    whenHatChanged(Hats.D_PAD) {
        when (it) {
            Direction.CENTER -> {
            }

            Direction.NORTH -> elevatorMachine.setState(ElevatorStates.POS_SCALE_HIGH)
            //Direction.EAST -> elevatorMachine.setState(ElevatorStates.POS_SWITCH)
            Direction.SOUTH -> elevatorMachine.setState(ElevatorStates.POS_DRIVE)
            Direction.WEST -> elevatorMachine.setState(ElevatorStates.POS_SCALE)
            Direction.EAST -> elevatorMachine.setState(ElevatorStates.POS_SWITCH)
            //Direction.WEST -> elevatorMachine.setState(ElevatorStates.POS_VAULT_RUNNER)
        }
    }

    //Intake control
    whenButton(Buttons.A) {
        pressed {
            if (elevatorMachine.getState() != ElevatorStates.POS_VAULT_RUNNER) {
                elevatorMachine.setState(ElevatorStates.GO_TO_COLLECTION)
            }
            Thread.sleep(100)
            elevatorClampMachine.setState(ElevatorClampStates.UNCLAMPED)
            intakeFolding.setState(IntakeFoldingStates.GO_TO_INTAKE)
            intakeWheels.setState(IntakeWheelsStates.REVERSE)
        }
        released {
            intakeWheels.setState(IntakeWheelsStates.IDLE)
            if (elevatorMachine.getState() == ElevatorStates.POS_COLLECTION) {
                elevatorMachine.setState(ElevatorStates.POS_DRIVE)
            }

            if(elevatorMachine.getState() != ElevatorStates.POS_VAULT_RUNNER){
                intakeFolding.setState(IntakeFoldingStates.STOWED)
            }
        }
    }

    //Intake
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
            if (elevatorMachine.getState() != ElevatorStates.POS_VAULT_RUNNER && !Elevator.estop) {
                if (intakeWheels.getState() != IntakeWheelsStates.IDLE) {
                    intakeWheels.setState(IntakeWheelsStates.INTAKE)
                }
                if (intakeFolding.getState() != IntakeFoldingStates.STOWED) {
                    intakeFolding.setState(IntakeFoldingStates.GRAB)
                }
            }
        }
    }

    //Grab
    whenButton(Buttons.Y) {
        pressed {
            elevatorClampMachine.setState(ElevatorClampStates.UNCLAMPED)
            elevatorMachine.setState(ElevatorStates.GO_TO_COLLECTION)
            intakeWheels.setState(IntakeWheelsStates.INTAKE)
            intakeFolding.setState(IntakeFoldingStates.GRAB)
            LED.signalWantCube()
        }
        released {
            intakeWheels.setState(IntakeWheelsStates.IDLE)
            if (elevatorMachine.getState() != ElevatorStates.POS_VAULT_RUNNER) {
                intakeFolding.setState(IntakeFoldingStates.STOWED)
            }
            if (elevatorMachine.getState() == ElevatorStates.POS_COLLECTION) {
                elevatorMachine.setState(ElevatorStates.POS_DRIVE)
            }
        }
    }

    //Reset
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
            LED.signalScoreCube()
        }

        released {
            elevatorKickerMachine.setState(ElevatorKickerStates.STOW)
        }
    }

    //Manual clamp
    whenButton(Buttons.START) {
        pressed {
            elevatorClampMachine.toggle(ElevatorClampStates.UNCLAMPED, ElevatorClampStates.CLAMPED)
        }
    }

    //Manual got cube
    whenButton(Buttons.LEFT_BUMPER) {
        pressed {
            elevatorClampMachine.setState(ElevatorClampStates.CLAMPED)
            intakeWheels.setState(IntakeWheelsStates.IDLE)
            intakeFolding.setState(IntakeFoldingStates.STOWED)
            elevatorMachine.setState(ElevatorStates.POS_DRIVE)
            LED.signalHaveCube()
        }
    }

    whenButton(Buttons.BACK) {
        pressed {
            elevatorMachine.setState(ElevatorStates.POS_VAULT_RUNNER)
        }
    }
}