package org.team401.robot2018.subsystems

import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.FeedbackDevice
import com.ctre.phoenix.motorcontrol.IMotorControllerEnhanced
import com.ctre.phoenix.motorcontrol.NeutralMode
import com.ctre.phoenix.motorcontrol.can.TalonSRX
import com.ctre.phoenix.sensors.PigeonIMU
import edu.wpi.first.wpilibj.PowerDistributionPanel
import edu.wpi.first.wpilibj.Solenoid
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import org.snakeskin.ShifterState
import org.snakeskin.component.Gearbox
import org.snakeskin.dsl.*
import org.snakeskin.component.TankDrivetrain
import org.snakeskin.event.Events
import org.team401.robot2018.LeftStick
//import org.team401.robot2018.MasherBox
import org.team401.robot2018.RightStick

//import org.team401.robot2018.LeftStick
//import org.team401.robot2018.RightStick
import org.team401.robot2018.*

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
    const val EXTERNAL_CONTROL = "nothing"
    const val OPEN_LOOP = "openloop"
    const val CHEESY = "cheesy"
    const val CHEESY_CLOSED = "betterCheesy"
}

const val DRIVE_SHIFT_MACHINE = "autoShifting"
object DriveShiftStates {
    const val HIGH = "high"
    const val LOW = "low"
    const val AUTO = "autoShifting"
}

val Drivetrain = TankDrivetrain(Constants.DrivetrainParameters.WHEEL_RADIUS, Constants.DrivetrainParameters.WHEELBASE)

val DrivetrainSubsystem: Subsystem = buildSubsystem("Drivetrain") {
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

        Drivetrain.setNeutralMode(NeutralMode.Coast)

    }

    val driveMachine = stateMachine(DRIVE_MACHINE) {
        //Empty state for when the drivetrain is being controlled by other processes
        state(DriveStates.EXTERNAL_CONTROL) {
            entry {
                Drivetrain.setRampRate(0.0, 0.0)
            }
        }

        //Shouldn't be used unless cheesy drive stops working for some reason
        state(DriveStates.OPEN_LOOP) {
            entry {
                Drivetrain.zero()
                Drivetrain.setNeutralMode(NeutralMode.Coast)

            }
            action {
                Drivetrain.arcade(
                        ControlMode.PercentOutput,
                        LeftStick.readAxis { PITCH },
                        RightStick.readAxis { ROLL }
                )
            }

        }

        //Totally our own control scheme and definitely not stolen from anywhere like team 254...
        state(DriveStates.CHEESY) {
            var quickTurn = false
            var pitch = 0.0
            var roll = 0.0

            fun cube(d: Double) = d*d*d

            val cheesyParameters = CheesyDriveParameters(
                    0.65,
                    0.5,
                    4.0,
                    0.65,
                    3.5,
                    4.0,
                    5.0,
                    0.95,
                    1.3,
                    0.2,
                    0.1,
                    5.0,
                    3,
                    2
            )

            val imuData = DoubleArray(3)

            entry {
                Drivetrain.zero()
                Drivetrain.setNeutralMode(NeutralMode.Coast)
                cheesyParameters.reset()
            }
            action {
                quickTurn = RightStick.readButton { TRIGGER }
                pitch = LeftStick.readAxis { PITCH }
                roll = RightStick.readAxis { ROLL }
                Drivetrain.cheesy(
                        ControlMode.PercentOutput,
                        cheesyParameters,
                        pitch,
                        if (quickTurn) cube(roll) else roll,
                        quickTurn
                        )
            }
        }

        state(DriveStates.CHEESY_CLOSED) {
            var quickTurn = false
            var pitch = 0.0
            var roll = 0.0

            fun cube(d: Double) = d*d*d

            val cheesyParameters = CheesyDriveParameters(
                    0.65,
                    0.5,
                    4.0,
                    0.65,
                    3.5,
                    4.0,
                    5.0,
                    0.95,
                    1.3,
                    0.2,
                    0.1,
                    5.0,
                    3,
                    2,
                    4500.0
            )

            entry {
                Drivetrain.zero()
                Drivetrain.setNeutralMode(NeutralMode.Coast)
                Drivetrain.setRampRate(.25, .25)
                cheesyParameters.reset()
            }

            action {
                quickTurn = RightStick.readButton { TRIGGER }
                pitch = LeftStick.readAxis { PITCH }
                roll = RightStick.readAxis { ROLL }
                Drivetrain.cheesy(
                        ControlMode.PercentOutput,
                        cheesyParameters,
                        pitch,
                        if (quickTurn) cube(roll) else roll,
                        quickTurn
                )
            }
        }

        state("testAccel") {
            var startTime = 0L
            var readingLeft = 0
            var readingRight = 0

            var error = 0.0
            val desired = 360
            val yaw = DoubleArray(3)

            //timeout(1500, DriveStates.OPEN_LOOP)
            entry {
                startTime = System.currentTimeMillis()
                readingLeft = 0
                readingRight = 0
                error = 0.0

                left.setPosition(0)
                right.setPosition(0)

                imu.setYaw(0.0, 0)
            }

            action {
                //Drivetrain.arcade(ControlMode.PercentOutput, 1.0, 0.0)
                println("running")

                imu.getYawPitchRoll(yaw)

                error = (desired - yaw[0])

                left.set(ControlMode.PercentOutput, -error/desired)
                right.set(ControlMode.PercentOutput, error/desired)

                readingLeft = Drivetrain.left.getPosition()
                readingRight = Drivetrain.right.getPosition()
            }

            exit {
                System.out.println("${yaw[0]},$readingLeft,$readingRight")

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
            entry {
                low()
            }
            action {
                val newState = AutoShifter.shiftAuto(
                        System.currentTimeMillis(),
                        Drivetrain.getCurrent(),
                        Drivetrain.getVelocity() * .0025566,
                        Drivetrain.shifterState)

                Drivetrain.shiftUpdate(newState)
            }
        }
    }

    fun testMotor(controller: IMotorControllerEnhanced, name: String): Boolean {
        left.unlink()
        right.unlink()
        controller.set(ControlMode.PercentOutput, 1.0)
        Thread.sleep(1000)

        val position = controller.getSelectedSensorPosition(0)
        val velocity = controller.getSelectedSensorVelocity(0)
        SmartDashboard.putNumber("$name Position", position.toDouble())
        SmartDashboard.putNumber("$name Velocity", velocity.toDouble())

        controller.set(ControlMode.PercentOutput, 0.0)

        left.link()
        right.link()

        return true //TODO pick a condition to test here
    }

    test ("Drivetrain leftFront") { testMotor(leftFront, "leftFront") }
    test ("Drivetrain leftMidF") { testMotor(leftMidF, "leftMidF") }
    test ("Drivetrain leftMidR") { testMotor(leftMidR, "leftMidR") }
    test ("Drivetrain leftRear") { testMotor(leftRear, "leftRear") }

    test ("Drivetrain rightFront") { testMotor(rightFront, "rightFront") }
    test ("Drivetrain rightMidF") { testMotor(rightMidF, "rightMidF") }
    test("Drivetrain rightMidR") { testMotor(rightMidR, "rightMidR") }
    test("Drivetrain rightRear") { testMotor(rightRear, "rightRear") }

    on (Events.TELEOP_ENABLED) {
        driveMachine.setState(DriveStates.CHEESY_CLOSED)
        shiftMachine.setState(DriveShiftStates.HIGH)
    }

    on (Events.AUTO_ENABLED) {
        driveMachine.setState(DriveStates.EXTERNAL_CONTROL)
        shiftMachine.setState(DriveShiftStates.HIGH)
    }
}

