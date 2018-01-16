package org.team401.robot2018.subsystems

import org.snakeskin.dsl.*

/*
 * 2018-Robot-Code - Created on 1/15/18
 * Author: Cameron Earle
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at project root
 */

/**
 * @author Cameron Earle
 * @version 1/15/18
 */

val ELEVATOR_MACHINE = "elevator"
object ElevatorStates {

}

val ElevatorSubsystem: Subsystem = buildSubsystem {
    stateMachine(ELEVATOR_MACHINE) {

    }
}