package org.team401.robot2018.auto

import org.snakeskin.dsl.machine
import org.team401.robot2018.Constants
import org.team401.robot2018.Sequences
import org.team401.robot2018.Signals
import org.team401.robot2018.auto.steps.AutoStep
import org.team401.robot2018.auto.steps.LambdaStep
import org.team401.robot2018.auto.steps.StateStep
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
    val DeployElevator = LambdaStep(Sequences::deployElevator)
    val HomeElevator = object : AutoStep() {
        override fun entry() {
            ElevatorSubsystem.machine(ELEVATOR_MACHINE).setState(ElevatorStates.HOMING)
        }

        override fun action() {
            done = Signals.elevatorHomed
        }

        override fun exit() {}
    }

    val HoldElevator = StateStep(ElevatorSubsystem, ELEVATOR_MACHINE, ElevatorStates.HOLD_POS_UNKNOWN)

    val ElevatorToGround = LambdaStep { Signals.elevatorPosition = Constants.ElevatorParameters.HOME_POS }
    val ElevatorToSwitch = LambdaStep { Signals.elevatorPosition = Constants.ElevatorParameters.SWITCH_POS }
    val ElevatorToScale = LambdaStep { Signals.elevatorPosition = Constants.ElevatorParameters.SCALE_POS_HIGH }
    val ElevatorKickerScore = StateStep(ElevatorSubsystem, ELEVATOR_KICKER_MACHINE, ElevatorKickerStates.KICK)
    val ElevatorKickerRetract = StateStep(ElevatorSubsystem, ELEVATOR_KICKER_MACHINE, ElevatorKickerStates.STOW)
    val ElevatorHolderClamp = StateStep(ElevatorSubsystem, ELEVATOR_CLAMP_MACHINE, ElevatorClampStates.CLAMPED)
    val ElevatorHolderUnclamp = StateStep(ElevatorSubsystem, ELEVATOR_CLAMP_MACHINE, ElevatorClampStates.UNCLAMPED)
}