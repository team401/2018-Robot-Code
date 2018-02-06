package org.team401.robot2018.subsystems

import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.can.TalonSRX
import edu.wpi.first.wpilibj.Solenoid
import edu.wpi.first.wpilibj.SpeedController
import edu.wpi.first.wpilibj.VictorSP
import org.snakeskin.component.MotorGroup
import org.snakeskin.dsl.*
import org.team401.robot2018.Constants
import org.team401.robot2018.Signals
import org.team401.robot2018.pidf

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
    const val GRAB = "wide"
    const val INTAKE = "out"
    const val STOWED = "in"
}

val IntakeSubsystem: Subsystem = buildSubsystem {
    val folding = TalonSRX(Constants.MotorControllers.INTAKE_FOLDING_CAN)

    val left = TalonSRX(Constants.MotorControllers.INTAKE_LEFT_CAN)
    val right = TalonSRX(Constants.MotorControllers.INTAKE_RIGHT_CAN)

    setup {
        right.inverted = true

        folding.configContinuousCurrentLimit(30, 0)
        //folding.pidf(0.0,0.0,0.0,0.0)
    }


    stateMachine(INTAKE_WHEELS_MACHINE) {
        state(IntakeWheelsStates.INTAKE) {
            action {
                left.set(ControlMode.PercentOutput, Constants.IntakeParameters.INTAKE_RATE)
                right.set(ControlMode.PercentOutput, Constants.IntakeParameters.INTAKE_RATE)

                    if(left.outputCurrent >= Constants.IntakeParameters.HAVE_CUBE_CURRENT &&
                            right.outputCurrent >= Constants.IntakeParameters.HAVE_CUBE_CURRENT) {
                        //Have cube
                        //Move elevator or whatever
                        //turn on LED's
                        Signals.elevatorPosition = Constants.ElevatorParameters.CUBE_POS
                    }
            }
        }

        state(IntakeWheelsStates.REVERSE) {
            action {
                left.set(ControlMode.PercentOutput, Constants.IntakeParameters.REVERSE_RATE)
                right.set(ControlMode.PercentOutput, Constants.IntakeParameters.REVERSE_RATE)
            }
        }

        state(IntakeWheelsStates.IDLE) {
            action {
                left.set(ControlMode.PercentOutput, 0.0)
                right.set(ControlMode.PercentOutput, 0.0)
            }
        }

        default {
            action {
                left.set(ControlMode.PercentOutput, 0.0)
                right.set(ControlMode.PercentOutput, 0.0)
            }
        }
    }

    stateMachine(INTAKE_FOLDING_MACHINE) {
        state(IntakeFoldingStates.GRAB) {
            entry {
                folding.set(ControlMode.Position, Constants.IntakeParameters.GRAB_POS)
            }
        }

        state(IntakeFoldingStates.INTAKE) {
            entry {
                folding.set(ControlMode.Position, Constants.IntakeParameters.INTAKE_POS)
            }
        }

        state(IntakeFoldingStates.STOWED) {
            entry {
                folding.set(ControlMode.Position, Constants.IntakeParameters.STOWED_POS)
            }
        }

        default {
            entry {
                folding.set(ControlMode.PercentOutput, 0.0)
            }
        }
    }
}