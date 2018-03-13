package org.team401.robot2018.subsystems

import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.FeedbackDevice
import com.ctre.phoenix.motorcontrol.can.TalonSRX
import edu.wpi.first.wpilibj.Servo
import org.snakeskin.dsl.*
import org.snakeskin.event.Events
import org.snakeskin.logic.History
import org.team401.robot2018.PDP
import org.team401.robot2018.constants.Constants
import org.team401.robot2018.etc.*

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

const val INTAKE_WHEELS_MACHINE = "intake"
object IntakeWheelsStates {
    const val INTAKE = "intake"
    const val REVERSE = "reverse"
    const val IDLE = "idle"
    const val GOT_CUBE = "gotem"
    const val HAVE_CUBE = "haveCube"
}

const val INTAKE_FOLDING_MACHINE = "intake_folding"
object IntakeFoldingStates {
    const val GRAB = "wide"
    const val INTAKE = "out"
    const val STOWED = "in"
    const val GO_TO_INTAKE = "goToIntake"
}

object Intake {
    lateinit var folding: TalonSRX
    lateinit var left: TalonSRX
    lateinit var right: TalonSRX

    fun stowed() = folding.getSelectedSensorPosition(0).toDouble().withinTolerance(Constants.IntakeParameters.STOWED_POS, 100.0)
    fun atGrab() = folding.getSelectedSensorPosition(0).toDouble().withinTolerance(Constants.IntakeParameters.GRAB_POS, 100.0)
}

var cubeCount = 0


val IntakeSubsystem: Subsystem = buildSubsystem {
    val folding = TalonSRX(Constants.MotorControllers.INTAKE_FOLDING_CAN)
    val camera = Servo(1)


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
                camera.set(1.0)
                folding.set(ControlMode.Position, Constants.IntakeParameters.GRAB_POS)
            }
        }

        state(IntakeFoldingStates.GO_TO_INTAKE) {
            action {
                if (Elevator.estop || Elevator.atCollection()) {
                    Thread.sleep(100)
                    setState(IntakeFoldingStates.INTAKE)
                }
            }
        }

        state(IntakeFoldingStates.INTAKE) {
            entry {
                camera.set(1.0)
                folding.set(ControlMode.Position, Constants.IntakeParameters.INTAKE_POS)
            }
        }

        state(IntakeFoldingStates.STOWED) {
            entry {
                camera.set(.65)
                folding.set(ControlMode.Position, Constants.IntakeParameters.STOWED_POS)
            }
        }

        default {
            entry {
                camera.set(0.0)
                folding.set(ControlMode.PercentOutput, 0.0)
            }
            action{
                //println("Intake Position: ${folding.getSelectedSensorPosition(0)}")
            }
        }
    }

    val intakeMachine = stateMachine(INTAKE_WHEELS_MACHINE) {
        state(IntakeWheelsStates.INTAKE) {
            var inrushCounter = 0
            entry {
                inrushCounter = 0
            }

            action {
                left.voltageCompensation(Constants.IntakeParameters.INTAKE_RATE, Constants.IntakeParameters.INTAKE_VOLTAGE)
                right.voltageCompensation(Constants.IntakeParameters.INTAKE_RATE, Constants.IntakeParameters.INTAKE_VOLTAGE)

                println("Intake Current: " + RobotMath.averageCurrent(left, right))

                if (inrushCounter < Constants.IntakeParameters.INRUSH_COUNT) {
                    inrushCounter++
                } else {
                    if (RobotMath.averageCurrent(left, right) >= Constants.IntakeParameters.HAVE_CUBE_CURRENT_INTAKE) {
                        setState(IntakeWheelsStates.GOT_CUBE)
                    }
                }
            }
        }

        state(IntakeWheelsStates.GOT_CUBE) {
            timeout(Constants.IntakeParameters.CUBE_HELD_TIME, IntakeWheelsStates.HAVE_CUBE)

            action {
                left.voltageCompensation(Constants.IntakeParameters.RETAIN_RATE, Constants.IntakeParameters.INTAKE_VOLTAGE)
                right.voltageCompensation(Constants.IntakeParameters.RETAIN_RATE, Constants.IntakeParameters.INTAKE_VOLTAGE)

                println("Got Current: " + RobotMath.averageCurrent(left, right))


                if (RobotMath.averageCurrent(left, right) < Constants.IntakeParameters.HAVE_CUBE_CURRENT_HOLD) {
                    setState(IntakeWheelsStates.INTAKE)
                }
            }
        }

        state(IntakeWheelsStates.HAVE_CUBE) {
            entry {
                left.set(ControlMode.PercentOutput, 0.0)
                right.set(ControlMode.PercentOutput, 0.0)
                send(RobotEvents.HAVE_CUBE)
                Thread.sleep(Constants.IntakeParameters.HAVE_CUBE_CLAMP_DELAY)

                if (!Elevator.estop) {
                    foldingMachine.setState(IntakeFoldingStates.STOWED)
                    setState(IntakeWheelsStates.IDLE)
                }

                cubeCount++
            }
        }

        state(IntakeWheelsStates.REVERSE) {
            entry{
                send(RobotEvents.EJECT_CUBE)
                Thread.sleep(100)
            }
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