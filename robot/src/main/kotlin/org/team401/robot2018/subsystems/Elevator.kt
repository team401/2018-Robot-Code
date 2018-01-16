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

val ELEVATOR_MACHINE = "elevator"
object ElevatorStates {
    const val SIGNAL_CONTROL = "signal"
    const val OPEN_LOOP_CONTROL = "openloop"
    const val HOLD = "pos_lock"
    const val HOMING = "homing"
}

val ELEVATOR_SHIFTER_MACHINE = "elevator_shifter"
object ElevatorShifterStates {
    const val RUN = "high"
    const val CLIMB = "low"
}

val ElevatorSubsystem: Subsystem = buildSubsystem {
    val elevatorMachine = stateMachine(ELEVATOR_MACHINE) {
        state(ElevatorStates.SIGNAL_CONTROL) {
            action {
                //TODO gearbox.set(ControlMode.MotionMagic, Signals.elevatorPosition)
            }
        }

        state(ElevatorStates.OPEN_LOOP_CONTROL) {
            action {
                //TODO gearbox.set(ControlMode.PercentOutput, MasherBox.readAxis { ... }
            }
        }

        state(ElevatorStates.HOLD) {
            entry {
                //TODO gearbox.setPosition(0.0)
            }

            action {
                //TODO gearbox.set(ControlMode.Position, 0.0)
            }
        }

        state(ElevatorStates.HOMING) {
            entry {
                //TODO gearbox.master.zeroOnLimit(true)
            }

            action {
                //TODO gearbox.set(ControlMode.PercentOutput, Constants.ElevatorParameters.HOMING_RATE)
            }

            exit {
                //TODO gearbox.master.zeroOnLimit(false)
            }
        }

        default {
            action {
                //TODO gearbox.stop()
            }
        }
    }

    val elevatorShifterMachine = stateMachine(ELEVATOR_SHIFTER_MACHINE) {
        state(ElevatorShifterStates.RUN) {
            entry {
                //TODO shift high
            }
        }

        state(ElevatorShifterStates.CLIMB) {
            entry {
                //TODO shift low
            }
        }

        default {
            entry {
                //TODO shifter.set(false)
            }
        }
    }

    on(Events.ENABLED) {
        elevatorMachine.setState(ElevatorStates.HOLD)
    }
}