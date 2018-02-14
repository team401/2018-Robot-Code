package org.team401.robot2018

import com.ctre.phoenix.motorcontrol.IMotorControllerEnhanced
import com.ctre.phoenix.motorcontrol.can.TalonSRX
import edu.wpi.first.wpilibj.PowerDistributionPanel
import edu.wpi.first.wpilibj.Sendable
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import org.snakeskin.ShifterState
import org.snakeskin.dsl.Publisher
import org.snakeskin.factory.ExecutorFactory
import org.team401.robot2018.subsystems.Drivetrain
import org.team401.robot2018.subsystems.Elevator
import org.team401.robot2018.subsystems.Intake
import org.team401.robot2018.subsystems.Rungs
import java.lang.reflect.Field
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
        val report = Report()

        report.run {
            val yawPitchRoll = DoubleArray(3)
            Drivetrain.imu.getYawPitchRoll(yawPitchRoll)
            yaw = yawPitchRoll[0]
            pitch = yawPitchRoll[1]
            driveLeftVelocity = UnitConversions.nativeUnitsToRpm(Drivetrain.left.getVelocity().toDouble())
            driveRightVelocity = UnitConversions.nativeUnitsToRpm(Drivetrain.right.getVelocity().toDouble())
            driveLeftPosition = UnitConversions.nativeUnitsToRevolutions(Drivetrain.left.getPosition().toDouble())
            driveRightPosition = UnitConversions.nativeUnitsToRevolutions(Drivetrain.right.getPosition().toDouble())
            driveLeftAmps = Drivetrain.left.getCurrent()
            driveRightAmps = Drivetrain.right.getCurrent()
            vbus = PDP.voltage
            totalAmps = PDP.totalCurrent
            intakePos = UnitConversions.nativeUnitsToRevolutions(Intake.folding.getSelectedSensorPosition(0).toDouble())
            intakeVelocity = UnitConversions.nativeUnitsToRpm(Intake.folding.getSelectedSensorVelocity(0).toDouble())
            intakeAmps = Intake.folding.outputCurrent
            intakeLeftAmps = Intake.left.outputCurrent
            intakeRightAmps = Intake.right.outputCurrent
            elevatorVelocity = Elevator.gearbox.getVelocity().toDouble()
            elevatorPosition = Elevator.gearbox.getPosition().toDouble()
            elevatorAmps = Elevator.gearbox.getCurrent(
                    Constants.PDPChannels.ELEVATOR_SLAVE_1_PDP,
                    Constants.PDPChannels.ELEVATOR_SLAVE_2_PDP,
                    Constants.PDPChannels.ELEVATOR_SLAVE_3_PDP
                    )
            kicker = Elevator.kicker.get()
            clamp = Elevator.clamp.get()
            elevatorLowerLimit = (Elevator.gearbox.master as TalonSRX).sensorCollection.isRevLimitSwitchClosed
            driveShift = Drivetrain.shifterState.toString().toLowerCase()
            elevatorShift = if (Elevator.shifter.get()) "high" else "low"
            elevatorDeploy = if (Elevator.deployer.get()) "unlocked" else "locked"
            elevatorRatchet = Elevator.ratchet.get()
            rungsDeploy = if (Rungs.deployer.get()) "unlocked" else "locked"
        }
    }

    fun start() {
        executor.scheduleAtFixedRate(this::update, 0L, Constants.ReportingParameters.REPORTING_RATE, TimeUnit.MILLISECONDS)
    }
}