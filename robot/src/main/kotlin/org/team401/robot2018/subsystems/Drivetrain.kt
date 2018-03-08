package org.team401.robot2018.subsystems

//import org.team401.robot2018.MasherBox

//import org.team401.robot2018.LeftStick
//import org.team401.robot2018.RightStick
import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.FeedbackDevice
import com.ctre.phoenix.motorcontrol.IMotorControllerEnhanced
import com.ctre.phoenix.motorcontrol.NeutralMode
import com.ctre.phoenix.motorcontrol.can.TalonSRX
import com.ctre.phoenix.sensors.PigeonIMU
import edu.wpi.first.wpilibj.Solenoid
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import org.snakeskin.ShifterState
import org.snakeskin.component.Gearbox
import org.snakeskin.component.TankDrivetrain
import org.snakeskin.dsl.CheesyDriveParameters
import org.snakeskin.dsl.Subsystem
import org.snakeskin.dsl.buildSubsystem
import org.snakeskin.dsl.cheesy
import org.snakeskin.event.Events
import org.snakeskin.logic.scalars.CubicScalar
import org.team401.robot2018.LeftStick
import org.team401.robot2018.RightStick
import org.team401.robot2018.constants.Constants
import org.team401.robot2018.etc.getCurrent
import org.team401.robot2018.etc.shiftUpdate
import org.team401.robot2018.getPitch

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
    const val TIP_CONTROL = "tipControl"
}

const val DRIVE_SHIFT_MACHINE = "autoShifting"
object DriveShiftStates {
    const val HIGH = "high"
    const val LOW = "low"
    const val AUTO = "autoShifting"
}

data class ShiftCommand(val state: ShifterState, val reason: String = "")

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

    fun shift(state: ShifterState) {
        when (state) {
            ShifterState.HIGH -> {
                Drivetrain.high()
            }

            ShifterState.LOW -> {
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
        Drivetrain.setCurrentLimit(
                Constants.DrivetrainParameters.CURRENT_LIMIT_CONTINUOUS,
                Constants.DrivetrainParameters.CURRENT_LIMIT_PEAK,
                Constants.DrivetrainParameters.CURRENT_LIMIT_TIMEOUT
        )

        Drivetrain.setRampRate(Constants.DrivetrainParameters.CLOSED_LOOP_RAMP, Constants.DrivetrainParameters.OPEN_LOOP_RAMP)

        Drivetrain.setNeutralMode(NeutralMode.Coast)
    }

    val driveMachine = stateMachine(DRIVE_MACHINE) {
        //Empty state for when the drivetrain is being controlled by other processes
        state(DriveStates.EXTERNAL_CONTROL) {}

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
                    quickTurnScalar = CubicScalar
            )

            entry {
                Drivetrain.zero()
                Drivetrain.setNeutralMode(NeutralMode.Coast)
                cheesyParameters.reset()
            }
            action {
                Drivetrain.cheesy(
                        ControlMode.PercentOutput,
                        cheesyParameters,
                        LeftStick.readAxis { PITCH },
                        RightStick.readAxis { ROLL },
                        RightStick.readButton { TRIGGER }
                )
            }
        }

        state(DriveStates.CHEESY_CLOSED) {
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
                    4200.0,
                    quickTurnScalar = CubicScalar
            )

            entry {
                Drivetrain.zero()
                Drivetrain.setNeutralMode(NeutralMode.Coast)
                Drivetrain.setRampRate(.25, .25)
                cheesyParameters.reset()
            }

            action {
                Drivetrain.cheesy(
                        ControlMode.Velocity,
                        cheesyParameters,
                        LeftStick.readAxis { PITCH },
                        RightStick.readAxis { ROLL },
                        RightStick.readButton { TRIGGER}
                )
            }
        }

        state(DriveStates.TIP_CONTROL) {
            var correction: Double
            action {
                correction = getPitch() / Constants.DrivetrainParameters.TIP_CORRECTION_SCALAR
                Drivetrain.arcade(ControlMode.PercentOutput, correction, 0.0)
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

            var lastShiftTime = System.currentTimeMillis()

            //currentTime and lastShiftTime in ms
            fun shiftAuto(currentTime: Long, currentAmpDraw: Double, currentVel: Double, currentGear: ShifterState): ShiftCommand {
                if (currentAmpDraw >= Constants.DrivetrainParameters.DOWNSHIFT_CURRENT) return ShiftCommand(ShifterState.LOW, "Overcurrent")

                if (currentTime - lastShiftTime <= 250) return ShiftCommand(currentGear, "Fast toggle")
                //high gear and low velocity likely means the robot is stuck on a wall, so we return low gear
                if (currentGear == ShifterState.HIGH && currentVel < Constants.DrivetrainParameters.SPEED_THRESHOLD) return ShiftCommand(ShifterState.LOW, "Underspeed")

                if (Constants.DrivetrainParameters.SPEED_SPLIT - currentVel > Constants.DrivetrainParameters.DELTA) return ShiftCommand(ShifterState.LOW, "Low speed")
                if (Constants.DrivetrainParameters.SPEED_SPLIT - currentVel < -Constants.DrivetrainParameters.DELTA) return ShiftCommand(ShifterState.HIGH, "High speed")
                //minor velocity change, so no gear shift
                return ShiftCommand(currentGear, "No cases satisfied")
            }

            fun update() {
                lastShiftTime = System.currentTimeMillis()
            }

            entry {
                low()
            }
            action {
                val newState = shiftAuto(
                        System.currentTimeMillis(),
                        Drivetrain.getCurrent(),
                        Drivetrain.getVelocity() * .0025566,
                        Drivetrain.shifterState)

                if (Drivetrain.shiftUpdate(newState)) update()
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
        driveMachine.setState(DriveStates.CHEESY)
        shiftMachine.setState(DriveShiftStates.HIGH)
    }

    on (Events.AUTO_ENABLED) {
        driveMachine.setState("nothing")
        shiftMachine.setState(DriveShiftStates.HIGH)
    }
}

