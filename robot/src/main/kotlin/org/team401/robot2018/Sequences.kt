package org.team401.robot2018

import org.snakeskin.dsl.machine
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
object Sequences {
    fun deployElevator() {
        ElevatorSubsystem.machine(ELEVATOR_MACHINE).setState(ElevatorStates.HOLD_POS_UNKNOWN)
        ElevatorSubsystem.machine(ELEVATOR_SHIFTER_MACHINE).setState(ElevatorShifterStates.HOLD_CARRIAGE)
        ElevatorSubsystem.machine(ELEVATOR_DEPLOY_MACHINE).setState(ElevatorDeployStates.DEPLOY)
    }
}