package org.team401.robot2018.auto

import org.team401.robot2018.Constants
import org.team401.robot2018.Sequences
import org.team401.robot2018.Signals
import org.team401.robot2018.auto.steps.LambdaStep
import org.team401.robot2018.auto.steps.SingleStep
import org.team401.robot2018.auto.steps.StateStep
import org.team401.robot2018.subsystems.ELEVATOR_MACHINE
import org.team401.robot2018.subsystems.ElevatorStates
import org.team401.robot2018.subsystems.ElevatorSubsystem

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


    val ElevatorToSwitch = LambdaStep({ Signals.elevatorPosition = Constants.ElevatorParameters.SWITCH_POS })
}