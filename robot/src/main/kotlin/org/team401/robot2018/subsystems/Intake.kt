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

val INTAKE_WHEELS_MACHINE = "intake"
object IntakeWheelsStates {
    const val INTAKE = "intake"
    const val REVERSE = "reverse"
    const val IDLE = "idle"
}

val INTAKE_FOLDING_MACHINE = "intake_folding"
object IntakeFoldingStates {
    const val DEPLOYED = "out"
    const val STOWED = "in"
}

val IntakeSubsystem: Subsystem = buildSubsystem {
    stateMachine(INTAKE_WHEELS_MACHINE) {
        state(IntakeWheelsStates.INTAKE) {
            action {
                //TODO motors.set(Constants.IntakeParameters.INTAKE_RATE)
            }
        }

        state(IntakeWheelsStates.REVERSE) {
            action {
                //TODO motors.set(Constants.IntakeParameters.INTAKE_RATE)
            }
        }

        state(IntakeWheelsStates.IDLE) {
            action {
                //TODO motors.set(0.0)
            }
        }

        default {
            action {
                //TODO motors.set(0.0)
            }
        }
    }

    stateMachine(INTAKE_FOLDING_MACHINE) {
        state(IntakeFoldingStates.DEPLOYED) {
            entry {
                //TODO deploy piston
            }
        }

        state(IntakeFoldingStates.STOWED) {
            entry {
                //TODO retract piston
            }
        }

        default {
            entry {
                //TODO retract piston
            }
        }
    }
}