package org.team401.robot2018.subsystems

import org.snakeskin.dsl.*
import org.snakeskin.event.Events

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

val RUNGS_MACHINE = "rungs"
object RungsStates {
    const val STOWED = "in"
    const val DEPLOYED = "out"
}

val RungsSubsystem: Subsystem = buildSubsystem {
    val rungsMachine = stateMachine(RUNGS_MACHINE) {
        state(RungsStates.DEPLOYED) {
            entry {
                //TODO deploy pistons
            }
        }

        state(RungsStates.STOWED) {
            rejectIf {
                isInState(RungsStates.DEPLOYED) //Disallow retracting if the rungs are out
            }

            entry {
                //TODO retract pistons
            }
        }

        default {
            entry {
                //TODO retract pistons
            }
        }
    }

    on(Events.ENABLED) {
        rungsMachine.setState(RungsStates.STOWED)
    }
}