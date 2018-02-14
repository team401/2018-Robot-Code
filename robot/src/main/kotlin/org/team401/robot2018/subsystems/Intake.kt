package org.team401.robot2018.subsystems

import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.FeedbackDevice
import com.ctre.phoenix.motorcontrol.can.TalonSRX
import org.snakeskin.dsl.*
import org.snakeskin.event.Events
import org.team401.robot2018.Constants
import org.team401.robot2018.RightStick
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

var cubeCount = 0


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

        folding.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Absolute, 0,0)
        folding.setSelectedSensorPosition(folding.getSelectedSensorPosition(0) % 4096, 0, 0)

        folding.configPeakOutputForward(.5, 0)
        folding.configPeakOutputReverse(-.5, 0)
        folding.enableCurrentLimit(true)
        folding.configContinuousCurrentLimit(10, 0)
        folding.configPeakCurrentLimit(30, 0)
        folding.configPeakCurrentDuration(100, 0)

        left.enableCurrentLimit(false)
        right.enableCurrentLimit(false)
        left.configPeakCurrentLimit(40, 0)
        right.configPeakCurrentLimit(40, 0)
        left.configContinuousCurrentLimit(30, 0)
        right.configContinuousCurrentLimit(30, 0)
        left.configPeakCurrentDuration(100, 0)
        right.configPeakCurrentDuration(100, 0)

        //folding.setSelectedSensorPosition(folding.sensorCollection.pulseWidthPosition % 4096,0, 0)

        /*
        folding.pidf(
                Constants.IntakeParameters.PIDF.P,
                Constants.IntakeParameters.PIDF.I,
                Constants.IntakeParameters.PIDF.D,
                Constants.IntakeParameters.PIDF.F)
    }*/
        //P = 3 D = 30

        left.configVoltageCompSaturation(Constants.IntakeParameters.INTAKE_VOLTAGE, 0)
        left.enableVoltageCompensation(false)

        right.configVoltageCompSaturation(Constants.IntakeParameters.INTAKE_VOLTAGE,0)
        right.enableVoltageCompensation(false)
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

            action {
                println("Intake:  present=${folding.sensorCollection.pulseWidthRiseToRiseUs != 0} pulsePos=${folding.sensorCollection.pulseWidthPosition} pos=${folding.getSelectedSensorPosition(0)}")
            }
        }
    }

    val intakeMachine = stateMachine(INTAKE_WHEELS_MACHINE) {

        state(IntakeWheelsStates.INTAKE) {
            action {
                left.set(ControlMode.PercentOutput, Constants.IntakeParameters.INTAKE_RATE)
                right.set(ControlMode.PercentOutput, Constants.IntakeParameters.INTAKE_RATE)

                println("INTAKE")

                /*
                    if(boxHeld()) {
                        //turn on LED's
                        cubeCount++

                        ElevatorSubsystem.machine(ELEVAOTR_CLAMP_MACHINE).setState(ElevatorClampStates.DEPLOYED)

                        setState(IntakeWheelsStates.IDLE)
                        foldingMachine.setState(IntakeFoldingStates.GRAB)

                        Thread.sleep(250)

                        Signals.elevatorPosition = Constants.ElevatorParameters.CUBE_POS
                    }else{
                        ElevatorSubsystem.machine(ELEVAOTR_CLAMP_MACHINE).setState(ElevatorClampStates.RETRACTED)
                        ElevatorSubsystem.machine(ELEVATOR_KICKER_MACHINE).setState(ElevatorKickerStates.STOW)
                        //elevator to get cube is button mashers responsibility
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

    on (Events.TELEOP_ENABLED) {
        foldingMachine.setState(IntakeFoldingStates.STOWED)
    }
}

fun boxHeld(): Boolean {
    return Intake.left.outputCurrent >= Constants.IntakeParameters.HAVE_CUBE_CURRENT &&
           Intake.right.outputCurrent >= Constants.IntakeParameters.HAVE_CUBE_CURRENT
}