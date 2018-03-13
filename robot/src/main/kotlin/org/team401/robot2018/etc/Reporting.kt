package org.team401.robot2018.etc

import com.ctre.phoenix.motorcontrol.can.TalonSRX
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import org.snakeskin.dsl.Publisher
import org.snakeskin.factory.ExecutorFactory
import org.snakeskin.logic.Timer
import org.team401.robot2018.PDP
import org.team401.robot2018.constants.Constants
import org.team401.robot2018.subsystems.Drivetrain
import org.team401.robot2018.subsystems.Elevator
import org.team401.robot2018.subsystems.Intake
import org.team401.robot2018.subsystems.Rungs
import java.util.concurrent.TimeUnit

/*
 * 2018-Robot-Code - Created on 2/12/18
 * Author: Cameron Earle
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at project root
 */

/**
 * @author Cameron Earle
 * @version 2/12/18
 */
object Reporting {
    private val executor = ExecutorFactory.getExecutor("reporting")
    private val timer = Timer()

    class Report {
        var yaw by Publisher(0.0)
        var pitch by Publisher(0.0)
        var driveLeftVelocity by Publisher(0.0)
        var driveRightVelocity by Publisher(0.0)
        var driveLeftPosition by Publisher(0.0)
        var driveRightPosition by Publisher(0.0)
        var driveLeftAmps by Publisher(0.0)
        var driveRightAmps by Publisher(0.0)
        var vbus by Publisher(0.0)
        var totalAmps by Publisher(0.0)
        var intakePos by Publisher(0.0)
        var intakeVelocity by Publisher(0.0)
        var intakeAmps by Publisher(0.0)
        var intakeLeftAmps by Publisher(0.0)
        var intakeRightAmps by Publisher(0.0)
        var elevatorVelocity by Publisher(0.0)
        var elevatorPosition by Publisher(0.0)
        var elevatorAmps by Publisher(0.0)
        var kicker by Publisher(false)
        var clamp by Publisher(false)
        var elevatorLowerLimit by Publisher(false)
        var driveShift by Publisher("low")
        var elevatorShift by Publisher("low")
        var elevatorDeploy by Publisher("locked")
        var elevatorRatchet by Publisher(false)
        var rungsDeploy by Publisher("locked")
    }

    fun update() {
        timer.start()

        val r = Report()

        val yawPitchRoll = DoubleArray(3)
        Drivetrain.imu.getYawPitchRoll(yawPitchRoll)
        r.yaw = yawPitchRoll[0]
        r.pitch = yawPitchRoll[1]
        r.driveLeftVelocity = RobotMath.UnitConversions.nativeUnitsToRpm(Drivetrain.left.getVelocity().toDouble())
        r.driveRightVelocity = RobotMath.UnitConversions.nativeUnitsToRpm(Drivetrain.right.getVelocity().toDouble())
        r.driveLeftPosition = RobotMath.UnitConversions.nativeUnitsToRevolutions(Drivetrain.left.getPosition().toDouble())
        r.driveRightPosition = RobotMath.UnitConversions.nativeUnitsToRevolutions(Drivetrain.right.getPosition().toDouble())
        r.driveLeftAmps = Drivetrain.left.getCurrent()
        r.driveRightAmps = Drivetrain.right.getCurrent()
        r.vbus = PDP.voltage
        r.totalAmps = PDP.totalCurrent

        r.intakePos = RobotMath.UnitConversions.nativeUnitsToRevolutions(Intake.folding.getSelectedSensorPosition(0).toDouble())
        r.intakeVelocity = RobotMath.UnitConversions.nativeUnitsToRpm(Intake.folding.getSelectedSensorVelocity(0).toDouble())
        r.intakeAmps = Intake.folding.outputCurrent
        r.intakeLeftAmps = Intake.left.outputCurrent
        r.intakeRightAmps = Intake.right.outputCurrent

        r.elevatorVelocity = Elevator.gearbox.getVelocity().toDouble()
        r.elevatorPosition = Elevator.gearbox.getPosition().toDouble()
        r.elevatorAmps = Elevator.gearbox.getCurrent(
                Constants.PDPChannels.ELEVATOR_SLAVE_1_PDP,
                Constants.PDPChannels.ELEVATOR_SLAVE_2_PDP,
                Constants.PDPChannels.ELEVATOR_SLAVE_3_PDP
        )

        r.kicker = Elevator.kicker.get()
        r.clamp = Elevator.clamp.get()
        r.elevatorLowerLimit = (Elevator.gearbox.master as TalonSRX).sensorCollection.isRevLimitSwitchClosed
        r.driveShift = Drivetrain.shifterState.toString().toLowerCase()
        r.elevatorShift = if (Elevator.shifter.get()) "high" else "low"
        r.elevatorDeploy = if (Elevator.deployer.get()) "unlocked" else "locked"
        r.elevatorRatchet = Elevator.ratchet.get() == Constants.ElevatorParameters.RATCHET_LOCKED_SERVO_POS
        r.rungsDeploy = if (Rungs.deployer.get()) "unlocked" else "locked"
    }

    private fun publishLimits() {
        SmartDashboard.putNumber("driveMinVelocity", Constants.DrivetrainParameters.MIN_VELOCITY)
        SmartDashboard.putNumber("driveMaxVelocity", Constants.DrivetrainParameters.MAX_VELOCITY)
        SmartDashboard.putNumber("maxDriveAmps", Constants.DrivetrainParameters.CURRENT_LIMIT_CONTINUOUS.toDouble())

        SmartDashboard.putNumber("intakeMinVelocity", Constants.IntakeParameters.FOLDING_MIN_VELOCITY)
        SmartDashboard.putNumber("intakeMaxVelocity", Constants.IntakeParameters.FOLDING_MAX_VELOCITY)
        SmartDashboard.putNumber("maxIntakeAmps", Constants.IntakeParameters.FOLDING_CONTINUOUS_LIMIT.toDouble())
        SmartDashboard.putNumber("maxIntakeLeftAmps", Constants.IntakeParameters.LEFT_CONTINUOUS_LIMIT.toDouble())
        SmartDashboard.putNumber("maxIntakeRightAmps", Constants.IntakeParameters.RIGHT_CONTINUOUS_LIMIT.toDouble())

        SmartDashboard.putNumber("elevatorMinVelocity", Constants.ElevatorParameters.MIN_VELOCITY)
        SmartDashboard.putNumber("elevatorMaxVelocity", Constants.ElevatorParameters.MAX_VELOCITY)
        SmartDashboard.putNumber("maxElevatorAmps", Constants.ElevatorParameters.CURRENT_LIMIT_CONTINUOUS.toDouble())
    }

    fun start() {
        publishLimits()
        executor.scheduleAtFixedRate(this::update, 0L, Constants.ReportingParameters.REPORTING_RATE, TimeUnit.MILLISECONDS)
    }
}