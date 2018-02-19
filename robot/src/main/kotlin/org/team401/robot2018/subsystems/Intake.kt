package org.team401.robot2018.subsystems

import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.FeedbackDevice
import com.ctre.phoenix.motorcontrol.can.TalonSRX
import org.snakeskin.dsl.Publisher
import org.snakeskin.dsl.Subsystem
import org.snakeskin.dsl.buildSubsystem
import org.snakeskin.dsl.machine
import org.snakeskin.event.Events
import org.team401.robot2018.PDP
import org.team401.robot2018.etc.Constants
import org.team401.robot2018.etc.pidf

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

        folding.configPeakOutputForward(Constants.IntakeParameters.FOLDING_PEAK_OUTPUT_FORWARD, 0)
        folding.configPeakOutputReverse(Constants.IntakeParameters.FOLDING_PEAK_OUTPUT_REVERSE, 0)
        folding.configContinuousCurrentLimit(Constants.IntakeParameters.FOLDING_CONTINUOUS_LIMIT, 0)
        folding.configPeakCurrentLimit(Constants.IntakeParameters.FOLDING_PEAK_LIMIT, 0)
        folding.configPeakCurrentDuration(Constants.IntakeParameters.FOLDING_PEAK_LIMIT_DUR, 0)

        folding.enableCurrentLimit(true)
        
        left.enableCurrentLimit(false)
        right.enableCurrentLimit(false)

        left.configPeakCurrentLimit(Constants.IntakeParameters.LEFT_PEAK_LIMIT, 0)
        right.configPeakCurrentLimit(Constants.IntakeParameters.RIGHT_PEAK_LIMIT, 0)

        left.configContinuousCurrentLimit(Constants.IntakeParameters.LEFT_CONTINUOUS_LIMIT, 0)
        right.configContinuousCurrentLimit(Constants.IntakeParameters.RIGHT_CONTINUOUS_LIMIT, 0)

        left.configPeakCurrentDuration(Constants.IntakeParameters.LEFT_PEAK_LIMIT_DUR, 0)
        right.configPeakCurrentDuration(Constants.IntakeParameters.RIGHT_PEAK_LIMIT_DUR, 0)


        folding.pidf(
                Constants.IntakeParameters.PIDF.P,
                Constants.IntakeParameters.PIDF.I,
                Constants.IntakeParameters.PIDF.D,
                Constants.IntakeParameters.PIDF.F)
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
            action{
                println("Pos ${folding.getSelectedSensorPosition(0)}")
            }
        }
    }

    val intakeMachine = stateMachine(INTAKE_WHEELS_MACHINE) {
        state(IntakeWheelsStates.INTAKE) {
            var counter = 0
            entry {
                counter = 0
            }
            action {
                left.set(ControlMode.PercentOutput, voltageCompensation(Constants.IntakeParameters.INTAKE_RATE))
                right.set(ControlMode.PercentOutput, voltageCompensation(Constants.IntakeParameters.INTAKE_RATE))

                if (boxHeld()){
                    counter++
                }else{
                    counter = 0
                }

                /*
                if(boxHeld() && counter >= Constants.IntakeParameters.CUBE_HELD_COUNT) {
                    //turn on LED's
                    cubeCount++

                        Thread.sleep(250)

                        ElevatorSubsystem.machine(ELEVATOR_MACHINE).setState(ElevatorStates.POS_DRIVE)

                        setState(IntakeWheelsStates.IDLE)
                        foldingMachine.setState(IntakeFoldingStates.GRAB)
                    } else{
                        ElevatorSubsystem.machine(ELEVATOR_CLAMP_MACHINE).setState(ElevatorClampStates.UNCLAMPED)
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
        //foldingMachine.setState("default")
        intakeMachine.setState(IntakeWheelsStates.IDLE)
    }
}

fun boxHeld(): Boolean {
    return Intake.left.outputCurrent >= Constants.IntakeParameters.HAVE_CUBE_CURRENT_L &&
           Intake.right.outputCurrent >= Constants.IntakeParameters.HAVE_CUBE_CURRENT_R
}
fun voltageCompensation(desiredOutput : Double) : Double{
    return desiredOutput * (Constants.IntakeParameters.INTAKE_VOLTAGE/ PDP.voltage)
}