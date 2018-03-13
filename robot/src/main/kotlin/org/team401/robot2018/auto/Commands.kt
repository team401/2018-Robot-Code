package org.team401.robot2018.auto

import org.snakeskin.dsl.machine
import org.team401.robot2018.auto.steps.*
import org.team401.robot2018.etc.RobotMath
import org.team401.robot2018.subsystems.*

/*
 * 2018-Robot-Code - Created on 1/23/18
 * Author: Cameron Earle
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at project root
 */

/**
 * @author Cameron Earle
 * @version 1/23/18
 */

object Commands {
    val DeployElevator = StateStep(ElevatorSubsystem, ELEVATOR_DEPLOY_MACHINE, ElevatorDeployStates.DEPLOY)
    val HomeElevator = object : AutoStep() {
        override fun entry() {
            ElevatorSubsystem.machine(ELEVATOR_MACHINE).setState(ElevatorStates.HOMING)
        }

        override fun action() {
            done = Elevator.homed
        }

        override fun exit() {}
    }

    val WaitForDeploy = WaitForStep {
        ElevatorSubsystem.machine(ELEVATOR_DEPLOY_MACHINE).getState() == ElevatorDeployStates.DEPLOYED
    }

    val WaitForHasCube = WaitForStep {
        ElevatorSubsystem.machine(ELEVATOR_MACHINE).getState() == ElevatorStates.POS_DRIVE
    }

    val WaitForAtSwitch = WaitForStep(Elevator::atSwitch)

    val HoldElevator = StateStep(ElevatorSubsystem, ELEVATOR_MACHINE, ElevatorStates.HOLD_POS_UNKNOWN)
    val ScaleAfterUnfold = StateStep(ElevatorSubsystem, ELEVATOR_MACHINE, ElevatorStates.SCALE_POS_UNKNOWN)

    val ElevatorToDrive = StateStep(ElevatorSubsystem, ELEVATOR_MACHINE, ElevatorStates.POS_DRIVE)
    val ElevatorToGround = StateStep(ElevatorSubsystem, ELEVATOR_MACHINE, ElevatorStates.POS_COLLECTION)
    val ElevatorToSwitch = StateStep(ElevatorSubsystem, ELEVATOR_MACHINE, ElevatorStates.POS_SWITCH)
    val ElevatorToScale = StateStep(ElevatorSubsystem, ELEVATOR_MACHINE, ElevatorStates.POS_SCALE_HIGH)
    val ElevatorKickerScore = StateStep(ElevatorSubsystem, ELEVATOR_KICKER_MACHINE, ElevatorKickerStates.KICK)
    val ElevatorKickerRetract = StateStep(ElevatorSubsystem, ELEVATOR_KICKER_MACHINE, ElevatorKickerStates.STOW)
    val ElevatorHolderClamp = StateStep(ElevatorSubsystem, ELEVATOR_CLAMP_MACHINE, ElevatorClampStates.CLAMPED)
    val ElevatorHolderUnclamp = StateStep(ElevatorSubsystem, ELEVATOR_CLAMP_MACHINE, ElevatorClampStates.UNCLAMPED)

    val ElevatorHigh = StateStep(ElevatorSubsystem, ELEVATOR_SHIFTER_MACHINE, ElevatorShifterStates.HIGH)

    val IntakeToStow = StateStep(IntakeSubsystem, INTAKE_FOLDING_MACHINE, IntakeFoldingStates.STOWED)
    val IntakeToGrab = StateStep(IntakeSubsystem, INTAKE_FOLDING_MACHINE, IntakeFoldingStates.GRAB)
    val IntakeToIntake = StateStep(IntakeSubsystem, INTAKE_FOLDING_MACHINE, IntakeFoldingStates.GO_TO_INTAKE)

    val IntakeWheelsIdle = StateStep(IntakeSubsystem, INTAKE_WHEELS_MACHINE, IntakeWheelsStates.IDLE)
    val IntakeWheelsRun = StateStep(IntakeSubsystem, INTAKE_WHEELS_MACHINE, IntakeWheelsStates.INTAKE)

    val HighLockDeployAndWait = arrayOf(ElevatorHigh, HoldElevator, DeployElevator, WaitForDeploy)

    val ZeroIMU = LambdaStep { Drivetrain.imu.setYaw(RobotMath.UnitConversions.degreesToCTREDumbUnit(90.0), 0); Thread.sleep(500) }
}