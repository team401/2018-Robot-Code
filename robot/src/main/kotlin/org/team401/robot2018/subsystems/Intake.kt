package org.team401.robot2018.subsystems

import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.FeedbackDevice
import com.ctre.phoenix.motorcontrol.can.TalonSRX
import edu.wpi.first.wpilibj.DigitalInput
import org.snakeskin.dsl.*
import org.snakeskin.event.Events
import org.snakeskin.logic.LockingDelegate
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
    const val HAVE_CUBE = "gotem"
}

const val INTAKE_FOLDING_MACHINE = "intake_folding"
object IntakeFoldingStates {
    const val GRAB = "wide"
    const val INTAKE_FORCE = "out"
    const val INTAKE = "intakeAuto"
    const val STOWED = "in"
    const val HOMING = "homing"
    const val GO_TO_INTAKE = "goToIntake"
}

object Intake {
    lateinit var folding: TalonSRX
    lateinit var left: TalonSRX
    lateinit var right: TalonSRX

    var homed by LockingDelegate(false)

    fun stowed() = folding.getSelectedSensorPosition(0).toDouble().withinTolerance(Constants.IntakeParameters.STOWED_POS, 100.0)
    fun atGrab() = folding.getSelectedSensorPosition(0).toDouble().withinTolerance(Constants.IntakeParameters.GRAB_POS, 100.0)
}

var cubeCount = 0


val IntakeSubsystem: Subsystem = buildSubsystem {
    val folding = TalonSRX(Constants.MotorControllers.INTAKE_FOLDING_CAN)

    val cubeLeft = DigitalInput(Constants.DIO.CUBE_LEFT_SENSOR)
    val cubeRight = DigitalInput(Constants.DIO.CUBE_RIGHT_SENSOR)
    val cubeBeamBreak = DigitalInput(Constants.DIO.CUBE_BEAM_BREAK)

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


        /*
        folding.pidf(
                Constants.IntakeParameters.PIDF.P,
                Constants.IntakeParameters.PIDF.I,
                Constants.IntakeParameters.PIDF.D,
                Constants.IntakeParameters.PIDF.F)
                */
    }


    val foldingMachine = stateMachine(INTAKE_FOLDING_MACHINE) {
        state(IntakeFoldingStates.GRAB) {
            var counter = 0

            entry {
                LED.intakeGrab()
                folding.set(ControlMode.Position, Constants.IntakeParameters.GRAB_POS)
                counter = 0
            }

            action {
                if (folding.getSelectedSensorPosition(0) >= Constants.IntakeParameters.PAST_ELEVATOR_RAIL_POS) {
                    if (cubeBeamBreak.get() == Constants.DIO.BEAM_BREAK_TRIGGERED) {
                        counter++
                    } else {
                        counter = 0
                    }
                    if (counter >= Constants.IntakeParameters.BEAM_BREAK_COUNT) {
                        setState(IntakeFoldingStates.INTAKE)
                    }
                }
            }
        }

        state(IntakeFoldingStates.GO_TO_INTAKE) {
            action {
                if (Elevator.estop || Elevator.atCollection() || ElevatorSubsystem.machine(ELEVATOR_MACHINE).getState() == ElevatorStates.POS_VAULT_RUNNER) {
                    Thread.sleep(100)
                    setState(IntakeFoldingStates.INTAKE_FORCE)
                }
            }
        }

        state(IntakeFoldingStates.INTAKE_FORCE) {
            entry {
                LED.intakeOut()
                folding.set(ControlMode.Position, Constants.IntakeParameters.INTAKE_POS)
            }
        }

        state(IntakeFoldingStates.INTAKE) {
            entry {
                LED.intakeOut()
                folding.set(ControlMode.Position, Constants.IntakeParameters.INTAKE_POS)
            }

            action {
                if (cubeBeamBreak.get() != Constants.DIO.BEAM_BREAK_TRIGGERED) {
                    setState(IntakeFoldingStates.GRAB)
                }
            }
        }

        state(IntakeFoldingStates.STOWED) {
            entry {
                LED.intakeRetract()
                folding.set(ControlMode.Position, Constants.IntakeParameters.STOWED_POS)
            }
        }

        state(IntakeFoldingStates.HOMING) {
            var homingCounter = 0
            var velocity: Int

            entry {
                Intake.homed = false
                homingCounter = 0
            }

            action {
                velocity = folding.getSelectedSensorVelocity(0)
                folding.set(ControlMode.PercentOutput, Constants.IntakeParameters.HOMING_RATE) //Run in at the homing rate

                if (velocity == 0) {
                    homingCounter++
                } else {
                    homingCounter = 0
                }

                if (homingCounter > Constants.IntakeParameters.HOMING_COUNT) {
                    folding.setSelectedSensorPosition(0, 0, 0)
                    Intake.homed = true
                    setState(IntakeFoldingStates.STOWED)
                }
            }
        }

        default {
            entry {
                folding.set(ControlMode.PercentOutput, 0.0)
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
                left.voltageCompensation(Constants.IntakeParameters.INTAKE_RATE_LEFT, Constants.IntakeParameters.INTAKE_VOLTAGE)
                right.voltageCompensation(Constants.IntakeParameters.INTAKE_RATE_RIGHT, Constants.IntakeParameters.INTAKE_VOLTAGE)

                if (cubeLeft.get() == Constants.DIO.CUBE_TRIGGERED && cubeRight.get() == Constants.DIO.CUBE_TRIGGERED) {
                    counter++
                } else {
                    counter = 0
                }

                if (counter >= Constants.IntakeParameters.HAVE_CUBE_COUNT) {
                    setState(IntakeWheelsStates.HAVE_CUBE)
                }
            }
        }

        state(IntakeWheelsStates.HAVE_CUBE) {
            entry {
                left.set(ControlMode.PercentOutput, 0.0)
                right.set(ControlMode.PercentOutput, 0.0)
                send(RobotEvents.HAVE_CUBE)
                Thread.sleep(Constants.IntakeParameters.HAVE_CUBE_CLAMP_DELAY)

                if (!Elevator.estop && ElevatorSubsystem.machine(ELEVATOR_MACHINE).getState() != ElevatorStates.POS_VAULT_RUNNER) {
                    foldingMachine.setState(IntakeFoldingStates.STOWED)
                }
                setState(IntakeWheelsStates.IDLE)
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

    on (Events.TELEOP_ENABLED) {
        if (!Intake.homed) {
            foldingMachine.setState(IntakeFoldingStates.HOMING)
        } else {
            foldingMachine.setState(IntakeFoldingStates.STOWED)
        }
        intakeMachine.setState(IntakeWheelsStates.IDLE)
    }
}