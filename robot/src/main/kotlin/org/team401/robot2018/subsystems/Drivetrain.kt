package org.team401.robot2018.subsystems

import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.FeedbackDevice
import com.ctre.phoenix.motorcontrol.NeutralMode
import com.ctre.phoenix.motorcontrol.can.TalonSRX
import com.ctre.phoenix.sensors.PigeonIMU
import edu.wpi.first.wpilibj.PowerDistributionPanel
import edu.wpi.first.wpilibj.Solenoid
import org.snakeskin.ShifterState
import org.snakeskin.component.Gearbox
import org.snakeskin.dsl.*
import org.snakeskin.component.TankDrivetrain
import org.snakeskin.event.Events
import org.team401.robot2018.Constants
import org.team401.robot2018.LeftStick
//import org.team401.robot2018.MasherBox
import org.team401.robot2018.RightStick
import org.team401.robot2018.pidf

//import org.team401.robot2018.LeftStick
//import org.team401.robot2018.RightStick

/*
 * 2018-Robot-Code - Created on 1/13/18
 * Author: Cameron Earle
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at project root
 */

/**
 * @author Cameron Earle
 * @version 1/13/18
 */

const val DRIVE_MACHINE = "drive"
object DriveStates {
    const val OPEN_LOOP = "openloop"
}

const val DRIVE_SHIFT_MACHINE = "shift"
object DriveShiftStates {
    const val HIGH = "high"
    const val LOW = "low"
    const val AUTO = "autoShifting"
}

val Drivetrain = TankDrivetrain(Constants.DrivetrainParameters.WHEEL_RADIUS, Constants.DrivetrainParameters.WHEELBASE)

val DrivetrainSubsystem: Subsystem = buildSubsystem {
    val leftFront = TalonSRX(Constants.MotorControllers.DRIVE_LEFT_FRONT_CAN)
    val leftMidF = TalonSRX(Constants.MotorControllers.DRIVE_LEFT_MIDF_CAN)
    val leftMidR = TalonSRX(Constants.MotorControllers.DRIVE_LEFT_MIDR_CAN)
    val leftRear = TalonSRX(Constants.MotorControllers.DRIVE_LEFT_REAR_CAN)
    val rightFront = TalonSRX(Constants.MotorControllers.DRIVE_RIGHT_FRONT_CAN)
    val rightMidF = TalonSRX(Constants.MotorControllers.DRIVE_RIGHT_MIDF_CAN)
    val rightMidR = TalonSRX(Constants.MotorControllers.DRIVE_RIGHT_MIDR_CAN)
    val rightRear = TalonSRX(Constants.MotorControllers.DRIVE_RIGHT_REAR_CAN)

    val left = Gearbox(leftFront, leftMidF, leftMidR, leftRear)
    val right = Gearbox(rightFront, rightMidF, rightMidR, rightRear)
    val imu = PigeonIMU(leftRear)

    val shifter = Solenoid(Constants.Pneumatics.SHIFTER_SOLENOID)

    val pdp = PowerDistributionPanel(0)

    fun shift(state: ShifterState) {
        when (state) {
            ShifterState.HIGH -> {
                Drivetrain.setCurrentLimit(
                        Constants.DrivetrainParameters.CURRENT_LIMIT_CONTINUOUS_HIGH,
                        Constants.DrivetrainParameters.CURRENT_LIMIT_PEAK_HIGH,
                        Constants.DrivetrainParameters.CURRENT_LIMIT_TIMEOUT_HIGH
                )
                Drivetrain.high()
            }

            ShifterState.LOW -> {
                Drivetrain.setCurrentLimit(
                        Constants.DrivetrainParameters.CURRENT_LIMIT_CONTINUOUS_LOW,
                        Constants.DrivetrainParameters.CURRENT_LIMIT_PEAK_LOW,
                        Constants.DrivetrainParameters.CURRENT_LIMIT_TIMEOUT_LOW
                )
                Drivetrain.low()
            }
        }
    }

    fun high() = shift(ShifterState.HIGH)
    fun low() = shift(ShifterState.LOW)

    setup {
        left.setSensor(FeedbackDevice.CTRE_MagEncoder_Absolute)
        right.setSensor(FeedbackDevice.CTRE_MagEncoder_Absolute)

        //left.master.pidf(f = .20431, p = .15)
        //right.master.pidf(f = .22133,p = .15)

        Drivetrain.init(left, right, imu, shifter, Constants.DrivetrainParameters.INVERT_LEFT, Constants.DrivetrainParameters.INVERT_RIGHT, Constants.DrivetrainParameters.INVERT_SHIFTER)
        Drivetrain.setRampRate(Constants.DrivetrainParameters.CLOSED_LOOP_RAMP, Constants.DrivetrainParameters.OPEN_LOOP_RAMP)
    }

    val driveMachine = stateMachine(DRIVE_MACHINE) {
        state(DriveStates.OPEN_LOOP) {
            entry {
                Drivetrain.zero()
                Drivetrain.setNeutralMode(NeutralMode.Coast)

            }
            action {
                Drivetrain.arcade(ControlMode.PercentOutput, LeftStick.readAxis { PITCH }, RightStick.readAxis { ROLL })//MasherBox.readAxis { LEFT_Y }, MasherBox.readAxis { RIGHT_X })
            }
        }

        state("testAccel") {
            var startTime = 0L
            var readingLeft = 0
            var readingRight = 0

            timeout(1500, DriveStates.OPEN_LOOP)
            entry {
                startTime = System.currentTimeMillis()
                readingLeft = 0
                readingRight = 0
            }

            action {
                Drivetrain.arcade(ControlMode.PercentOutput, 1.0, 0.0)
                readingLeft = Drivetrain.left.getVelocity()
                readingRight = Drivetrain.right.getVelocity()
            }

            exit {
                System.out.println("${pdp.voltage},$readingLeft,$readingRight")

                Drivetrain.stop()
            }
        }

        default {
            entry {
                Drivetrain.stop()
            }
        }
    }

    val shiftMachine = stateMachine(DRIVE_SHIFT_MACHINE) {
        state(DriveShiftStates.HIGH) {
            entry {
                high()
            }
        }

        state(DriveShiftStates.LOW) {
            entry {
                low()
            }
        }

        state(DriveShiftStates.AUTO) {
            action {
                //TODO auto shift code
            }
        }
    }

    on (Events.ENABLED) {
        driveMachine.setState(DriveStates.OPEN_LOOP)
        shiftMachine.setState(DriveShiftStates.HIGH)
    }
}

