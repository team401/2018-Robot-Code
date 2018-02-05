package org.team401.robot2018.subsystems

import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.can.TalonSRX
import edu.wpi.first.wpilibj.Solenoid
import edu.wpi.first.wpilibj.SpeedController
import edu.wpi.first.wpilibj.VictorSP
import org.snakeskin.component.MotorGroup
import org.snakeskin.dsl.*
import org.team401.robot2018.Constants

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

    //val motors = MotorGroup(left, right)

    setup {
        right.inverted = true
    }


    stateMachine(INTAKE_WHEELS_MACHINE) {
        state(IntakeWheelsStates.INTAKE) {
            action {
                //motors.set(Constants.IntakeParameters.INTAKE_RATE)
                left.set(ControlMode.PercentOutput, Constants.IntakeParameters.INTAKE_RATE)
                right.set(ControlMode.PercentOutput, Constants.IntakeParameters.INTAKE_RATE)

                    if(left.outputCurrent >= Constants.IntakeParameters.HAVE_CUBE_CURRENT &&
                            right.outputCurrent >= Constants.IntakeParameters.HAVE_CUBE_CURRENT) {
                        //Have cube
                        //Move elevator or whatever
                        //turn on LED's
                    }
            }
        }

        state(IntakeWheelsStates.REVERSE) {
            action {
                //motors.set(Constants.IntakeParameters.REVERSE_RATE)
                left.set(ControlMode.PercentOutput, Constants.IntakeParameters.REVERSE_RATE)
                right.set(ControlMode.PercentOutput, Constants.IntakeParameters.REVERSE_RATE)
            }
        }

        state(IntakeWheelsStates.IDLE) {
            action {
                //motors.set(0.0)
                left.set(ControlMode.PercentOutput, 0.0)
                right.set(ControlMode.PercentOutput, 0.0)
            }
        }

        default {
            action {
                //motors.set(0.0)
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