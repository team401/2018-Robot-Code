package org.team401.robot2018

import com.ctre.phoenix.motorcontrol.can.TalonSRX
import edu.wpi.first.wpilibj.PowerDistributionPanel
import edu.wpi.first.wpilibj.Sendable
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import org.snakeskin.ShifterState
import org.team401.robot2018.subsystems.Drivetrain
import org.team401.robot2018.subsystems.Elevator
import org.team401.robot2018.subsystems.Intake
import org.team401.robot2018.subsystems.Rungs
import java.lang.reflect.Field

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
    private fun fusedCurrent(vararg channels: Int) = channels.map { PDP.getCurrent(it) }.average()
    class Report {
        var yaw = 0.0
        var pitch = 0.0
        var driveLeftVelocity = 0.0
        var driveRightVelocity = 0.0
        var driveLeftPosition = 0.0
        var driveRightPosition = 0.0
        var driveLeftAmps = 0.0
        var driveRightAmps = 0.0
        var vbus = 0.0
        var totalAmps = 0.0
        var intakePos = 0.0
        var intakeVelocity = 0.0
        var intakeAmps = 0.0
        var intakeLeftAmps = 0.0
        var intakeRightAmps = 0.0
        var elevatorVelocity = 0.0
        var elevatorPosition = 0.0
        var elevatorAmps = 0.0
        var kicker = false
        var clamp = false
        var elevatorLowerLimit = false
        var driveShift = "low"
        var elevatorShift = "low"
        var elevatorDeploy = "locked"
        var elevatorRatchet = false
        var rungsDeploy = "locked"

        fun publishToSD() {
            val fields = javaClass.declaredFields
            fields.forEach {
                when (it.type) {
                    Double::class.java -> SmartDashboard.putNumber(it.name, it.get(this) as Double)
                    Int::class.java -> SmartDashboard.putNumber(it.name, (it.get(this) as Int).toDouble())
                    Boolean::class.java -> SmartDashboard.putBoolean(it.name, it.get(this) as Boolean)
                    String::class.java -> SmartDashboard.putString(it.name, it.get(this) as String)
                }
            }
        }
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
            driveLeftAmps = fusedCurrent(
                    Constants.PDPChannels.DRIVE_LEFT_FRONT_PDP,
                    Constants.PDPChannels.DRIVE_LEFT_MIDF_PDP,
                    Constants.PDPChannels.DRIVE_LEFT_MIDR_PDP,
                    Constants.PDPChannels.DRIVE_LEFT_REAR_PDP
            )
            driveRightAmps = fusedCurrent(
                    Constants.PDPChannels.DRIVE_RIGHT_FRONT_PDP,
                    Constants.PDPChannels.DRIVE_RIGHT_MIDF_PDP,
                    Constants.PDPChannels.DRIVE_RIGHT_MIDR_PDP,
                    Constants.PDPChannels.DRIVE_RIGHT_REAR_PDP
            )
            vbus = PDP.voltage
            totalAmps = PDP.totalCurrent
            intakePos = UnitConversions.nativeUnitsToRevolutions(Intake.folding.getSelectedSensorPosition(0).toDouble())
            intakeVelocity = UnitConversions.nativeUnitsToRpm(Intake.folding.getSelectedSensorVelocity(0).toDouble())
            intakeAmps = PDP.getCurrent(Constants.PDPChannels.INTAKE_FOLDING_PDP)
            intakeLeftAmps = PDP.getCurrent(Constants.PDPChannels.INTAKE_LEFT_PDP)
            intakeRightAmps = PDP.getCurrent(Constants.PDPChannels.INTAKE_RIGHT_PDP)
            elevatorVelocity = Elevator.gearbox.getVelocity().toDouble()
            elevatorPosition = Elevator.gearbox.getPosition().toDouble()
            elevatorAmps = fusedCurrent(
                    Constants.PDPChannels.ELEVATOR_MASTER_PDP,
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

        report.publishToSD()
    }
}