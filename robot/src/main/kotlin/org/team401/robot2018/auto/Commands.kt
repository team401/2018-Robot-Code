package org.team401.robot2018.auto

import org.snakeskin.dsl.machine
import org.team401.robot2018.auto.motionprofile.HeadingTracker
import org.team401.robot2018.auto.steps.*
import org.team401.robot2018.etc.LED
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
    fun DeployElevator() = StateStep(ElevatorSubsystem, ELEVATOR_DEPLOY_MACHINE, ElevatorDeployStates.DEPLOY)
    fun HomeElevator() = object : AutoStep() {
        override fun entry(currentTime: Double) {
            ElevatorSubsystem.machine(ELEVATOR_MACHINE).setState(ElevatorStates.HOMING)
        }

        override fun action(currentTime: Double, lastTime: Double) {
            done = Elevator.homed
        }

        override fun exit(currentTime: Double) {}
    }

    fun WaitForDeploy() = WaitForStep {
        ElevatorSubsystem.machine(ELEVATOR_DEPLOY_MACHINE).getState() == ElevatorDeployStates.DEPLOYED
    }

    fun WaitForHasCube() = WaitForStep {
        ElevatorSubsystem.machine(ELEVATOR_MACHINE).getState() == ElevatorStates.POS_DRIVE
    }


    fun WaitForAtSwitch() = WaitForStep(Elevator::atSwitch)

    fun HoldElevator() = StateStep(ElevatorSubsystem, ELEVATOR_MACHINE, ElevatorStates.HOLD_POS_UNKNOWN)
    fun ScaleAfterUnfold() = StateStep(ElevatorSubsystem, ELEVATOR_MACHINE, ElevatorStates.SCALE_POS_UNKNOWN)

    fun ElevatorToDrive() = StateStep(ElevatorSubsystem, ELEVATOR_MACHINE, ElevatorStates.POS_DRIVE)
    fun ElevatorToGround() = StateStep(ElevatorSubsystem, ELEVATOR_MACHINE, ElevatorStates.POS_COLLECTION)
    fun ElevatorToSwitch() = StateStep(ElevatorSubsystem, ELEVATOR_MACHINE, ElevatorStates.POS_SWITCH)
    fun ElevatorToScale() = StateStep(ElevatorSubsystem, ELEVATOR_MACHINE, ElevatorStates.POS_SCALE_HIGH)
    fun ElevatorKickerScore() = StateStep(ElevatorSubsystem, ELEVATOR_KICKER_MACHINE, ElevatorKickerStates.KICK)
    fun ElevatorKickerRetract() = StateStep(ElevatorSubsystem, ELEVATOR_KICKER_MACHINE, ElevatorKickerStates.STOW)
    fun ElevatorHolderClamp() = StateStep(ElevatorSubsystem, ELEVATOR_CLAMP_MACHINE, ElevatorClampStates.CLAMPED)
    fun ElevatorHolderUnclamp() = StateStep(ElevatorSubsystem, ELEVATOR_CLAMP_MACHINE, ElevatorClampStates.UNCLAMPED)

    fun ElevatorHigh() = StateStep(ElevatorSubsystem, ELEVATOR_SHIFTER_MACHINE, ElevatorShifterStates.HIGH)

    fun IntakeToStow() = StateStep(IntakeSubsystem, INTAKE_FOLDING_MACHINE, IntakeFoldingStates.HOMING)
    fun IntakeToGrab() = StateStep(IntakeSubsystem, INTAKE_FOLDING_MACHINE, IntakeFoldingStates.GRAB)
    fun IntakeToIntake() = StateStep(IntakeSubsystem, INTAKE_FOLDING_MACHINE, IntakeFoldingStates.GO_TO_INTAKE)

    fun IntakeWheelsIdle() = StateStep(IntakeSubsystem, INTAKE_WHEELS_MACHINE, IntakeWheelsStates.IDLE)
    fun IntakeWheelsRun() = StateStep(IntakeSubsystem, INTAKE_WHEELS_MACHINE, IntakeWheelsStates.INTAKE)

    fun HighLockDeployAndWait() = arrayOf(DelayStep(.5), ElevatorHigh(), HoldElevator(), DeployElevator(), WaitForDeploy())
    fun UnhomeElevator() = LambdaStep { Elevator.homed = false }

    fun Score() = arrayOf(ElevatorHolderUnclamp(), ElevatorKickerScore(), LambdaStep { LED.signalScoreCube() }, DelayStep(AutoDelays.SCORE_DELAY), ElevatorKickerRetract())

    fun ResetHeading() = LambdaStep { HeadingTracker.reset() }
}