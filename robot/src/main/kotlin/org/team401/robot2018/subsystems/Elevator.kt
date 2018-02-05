package org.team401.robot2018.subsystems

import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.LimitSwitchNormal
import com.ctre.phoenix.motorcontrol.LimitSwitchSource
import com.ctre.phoenix.motorcontrol.SensorCollection
import com.ctre.phoenix.motorcontrol.can.TalonSRX
import edu.wpi.first.wpilibj.Solenoid
import org.snakeskin.component.Gearbox
import org.snakeskin.dsl.*
import org.team401.robot2018.Constants
//import org.team401.robot2018.MasherBox
import org.team401.robot2018.Signals
import org.team401.robot2018.configZeroPosOnReverseLimit

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

val ELEVATOR_DEPLOY_MACHINE = "elevator_deploy"
object ElevatorDeployStates {
    const val STOWED = "stowed"
    const val DEPLOY = "deploy"
    const val DEPLOYED = "deployed"
}

val ELEVATOR_MACHINE = "elevator"
object ElevatorStates {
    const val SIGNAL_CONTROL = "signal"
    const val OPEN_LOOP_CONTROL = "openloop"
    const val MANUAL_ADJUSTMENT = "closedloop"
    const val HOLD_POS_UNKNOWN = "pos_lock"
    const val HOMING = "homing"
}

val ELEVATOR_SHIFTER_MACHINE = "elevator_shifter"
object ElevatorShifterStates {
    const val RUN = "high"
    const val CLIMB = "low"
    const val HOLD_CARRIAGE = "hold_carriage"
}

val ELEVATOR_RATCHET_MACHINE = "elevator_ratchet"
object ElevatorRatchetStates {
    const val LOCKED = "locked"
    const val UNLOCKED = "unlocked"
}

val ELEVATOR_KICKER_MACHINE = "elevator_kicker"
object ElevatorKickerStates {
    const val KICK = "out"
    const val STOW = "in"
}

val ElevatorSubsystem: Subsystem = buildSubsystem {
    val master = TalonSRX(Constants.MotorControllers.ELEVATOR_MASTER_CAN)
    val slave1 = TalonSRX(Constants.MotorControllers.ELEVATOR_SLAVE_1_CAN)
    val slave2 = TalonSRX(Constants.MotorControllers.ELEVATOR_SLAVE_2_CAN)
    val slave3 = TalonSRX(Constants.MotorControllers.ELEVATOR_SLAVE_3_CAN)

    val gearbox = Gearbox(master, slave1, slave2, slave3)

    val shifter = Solenoid(Constants.Pneumatics.ELEVATOR_SHIFTER_SOLENOID)
    val deployer = Solenoid(Constants.Pneumatics.ELEVATOR_DEPLOY_SOLENOID)
    val ratchet = Solenoid(Constants.Pneumatics.ELEVATOR_RATCHET_SOLENOID)
    val kicker = Solenoid(Constants.Pneumatics.ELEVATOR_KICKER_SOLENOID)

    setup {
        gearbox.setCurrentLimit(Constants.ElevatorParameters.CURRENT_LIMIT_CONTINUOUS)
        master.configReverseLimitSwitchSource(LimitSwitchSource.FeedbackConnector, LimitSwitchNormal.NormallyOpen, 10)
        master.configForwardSoftLimitThreshold(Constants.ElevatorParameters.MAX_POS, 10)
        master.configForwardSoftLimitEnable(true, 10)
    }

    val elevatorDeployMachine = stateMachine(ELEVATOR_DEPLOY_MACHINE) {
        //Constants for setting solenoid polarity
        val locked = false
        val unlocked = true

        state(ElevatorDeployStates.STOWED) {
            entry {
                deployer.set(locked)
            }
        }

        state(ElevatorDeployStates.DEPLOY) {
            timeout(Constants.ElevatorParameters.DEPLOY_TIMER, ElevatorDeployStates.DEPLOYED)

            entry {
                deployer.set(unlocked)
            }
        }

        state(ElevatorDeployStates.DEPLOYED) {
            entry {
                deployer.set(locked)
            }
        }

        default {
            entry {
                deployer.set(false)
            }
        }
    }

    val elevatorMachine = stateMachine(ELEVATOR_MACHINE) {
        /**
         * Takes the elevator to the position specified by its control signal
         */
        fun toSignal() {
            gearbox.set(ControlMode.MotionMagic, Signals.elevatorPosition)
        }

        state(ElevatorStates.SIGNAL_CONTROL) {
            rejectIf { elevatorDeployMachine.getState() != ElevatorDeployStates.DEPLOYED }

            action {
                toSignal()
            }
        }

        state(ElevatorStates.OPEN_LOOP_CONTROL) {
            rejectIf { elevatorDeployMachine.getState() != ElevatorDeployStates.DEPLOYED }

            action {
                gearbox.set(ControlMode.PercentOutput, 0.0)//MasherBox.readAxis { PITCH_BLUE })
            }
        }

        state(ElevatorStates.MANUAL_ADJUSTMENT) {
            rejectIf { elevatorDeployMachine.getState() != ElevatorDeployStates.DEPLOYED}

            var adjustment: Double
            action {
                adjustment = Constants.ElevatorParameters.MANUAL_RATE * 0.0//MasherBox.readAxis { PITCH_BLUE }
                Signals.elevatorPosition += adjustment
                toSignal()
            }
        }

        state(ElevatorStates.HOLD_POS_UNKNOWN) {
            entry {
                gearbox.setPosition(0)
            }

            action {
                gearbox.set(ControlMode.Position, 0.0)
            }
        }

        state(ElevatorStates.HOMING) {
            rejectIf { elevatorDeployMachine.getState() != ElevatorDeployStates.DEPLOYED }

            entry {
                master.configZeroPosOnReverseLimit(true)
                Signals.elevatorHomed = false
            }

            var sensorData: SensorCollection
            action {
                sensorData = master.sensorCollection
                gearbox.set(ControlMode.PercentOutput, Constants.ElevatorParameters.HOMING_RATE)
                if (sensorData.isRevLimitSwitchClosed) {
                    gearbox.stop()
                    Signals.elevatorPosition = 0.0
                    Signals.elevatorHomed = true
                    setState(ElevatorStates.SIGNAL_CONTROL)
                }
            }

            exit {
                master.configZeroPosOnReverseLimit(false)
            }
        }

        default {
            action {
                gearbox.stop()
            }
        }
    }

    val elevatorShifterMachine = stateMachine(ELEVATOR_SHIFTER_MACHINE) {
        //Constants for setting solenoid polarity
        val high = true
        val low = false
        val hold = false

        state(ElevatorShifterStates.RUN) {
            entry {
                shifter.set(high)
            }
        }

        state(ElevatorShifterStates.CLIMB) {
            entry {
                shifter.set(low)
            }
        }

        state(ElevatorShifterStates.HOLD_CARRIAGE) {
            entry {
                shifter.set(hold)
            }
        }

        default {
            entry {
                shifter.set(false)
            }
        }
    }

    val elevatorRatchetMachine = stateMachine(ELEVATOR_RATCHET_MACHINE) {
        //Constants for setting solenoid polarity
        val locked = true
        val unlocked = false

        state(ElevatorRatchetStates.LOCKED) {
            entry {
                ratchet.set(locked)
            }
        }

        state(ElevatorRatchetStates.UNLOCKED) {
            entry {
                ratchet.set(unlocked)
            }
        }

        default {
            entry {
                ratchet.set(false)
            }
        }
    }

    val elevatorKickerMachine = stateMachine(ELEVATOR_KICKER_MACHINE) {
        //Constants for setting solenoid polarity
        val extended = true
        val retracted = false

        state(ElevatorKickerStates.KICK) {
            entry {
                kicker.set(extended)
            }
        }

        state(ElevatorKickerStates.STOW) {
            entry {
                kicker.set(retracted)
            }
        }

        default {
            entry {
                kicker.set(false)
            }
        }
    }
}