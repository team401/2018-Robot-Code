package org.team401.robot2018.subsystems

import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.can.TalonSRX
import org.snakeskin.dsl.*
import org.team401.robot2018.Constants
import org.team401.robot2018.PDP
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

object Intake {
    lateinit var folding: TalonSRX
    lateinit var left: TalonSRX
    lateinit var right: TalonSRX
}

val IntakeSubsystem: Subsystem = buildSubsystem {
    val folding = TalonSRX(Constants.MotorControllers.INTAKE_FOLDING_CAN)

    val left = TalonSRX(Constants.MotorControllers.INTAKE_LEFT_CAN)
    val right = TalonSRX(Constants.MotorControllers.INTAKE_RIGHT_CAN)

    setup {
        Intake.folding = folding
        Intake.left = left
        Intake.right = right

        left.inverted = Constants.IntakeParameters.INVERT_LEFT
        right.inverted = Constants.IntakeParameters.INVERT_RIGHT

        folding.configPeakOutputForward(Constants.IntakeParameters.VOLTAGE_LIMIT, 0)
        folding.configPeakOutputReverse(-Constants.IntakeParameters.VOLTAGE_LIMIT, 0)

        //folding.configContinuousCurrentLimit(30, 0)
        /*
        folding.pidf(
                Constants.IntakeParameters.PIDF.P,
                Constants.IntakeParameters.PIDF.I,
                Constants.IntakeParameters.PIDF.D,
                Constants.IntakeParameters.PIDF.F)
                */

        left.configVoltageCompSaturation(Constants.IntakeParameters.INTAKE_VOLTAGE, 0)
        left.enableVoltageCompensation(true)

        right.configVoltageCompSaturation(Constants.IntakeParameters.INTAKE_VOLTAGE,0)
        right.enableVoltageCompensation(true)
    }

    val intakeMachine = stateMachine(INTAKE_WHEELS_MACHINE) {

        state(IntakeWheelsStates.INTAKE) {
            action {
                left.set(ControlMode.PercentOutput, Constants.IntakeParameters.INTAKE_RATE)
                right.set(ControlMode.PercentOutput, Constants.IntakeParameters.INTAKE_RATE)

                /*
                    if(boxHeld()) {
                        //Have cube
                        //Move elevator or whatever
                        //turn on LED's
                        Signals.elevatorPosition = Constants.ElevatorParameters.CUBE_POS
                    }
                    */
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

    val foldingMachine = stateMachine(INTAKE_FOLDING_MACHINE) {
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
    test("Folding Machine"){
        //test folding
        foldingMachine.setState(IntakeFoldingStates.STOWED)
        Thread.sleep(1000)

        foldingMachine.setState(IntakeFoldingStates.GRAB)
        Thread.sleep(1000)

        foldingMachine.setState(IntakeFoldingStates.INTAKE)
        Thread.sleep(1000)

        true//TODO fix later
    }
    test("Intake Machine"){
        //test each sides motors
        left.set(ControlMode.PercentOutput, 1.0)
        Thread.sleep(2000)
        var leftVoltage by Publisher(0.0)
        var leftCurrent by Publisher(0.0)
        leftVoltage = left.motorOutputVoltage
        leftCurrent = left.outputCurrent

        left.set(ControlMode.PercentOutput, 0.0)
        Thread.sleep(1000)

        right.set(ControlMode.PercentOutput, 1.0)
        Thread.sleep(2000)
        var rightVoltage by Publisher(0.0)
        var rightCurrent by Publisher(0.0)
        rightVoltage = right.motorOutputVoltage
        rightCurrent = right.outputCurrent

        right.set(ControlMode.PercentOutput, 0.0)
        Thread.sleep(1000)

        leftCurrent + rightCurrent + 5.0 >= leftCurrent
    }
}

fun boxHeld(): Boolean {
    return Intake.left.outputCurrent >= Constants.IntakeParameters.HAVE_CUBE_CURRENT &&
           Intake.right.outputCurrent >= Constants.IntakeParameters.HAVE_CUBE_CURRENT
}