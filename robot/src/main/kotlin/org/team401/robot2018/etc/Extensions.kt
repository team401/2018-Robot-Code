package org.team401.robot2018.etc

import com.ctre.phoenix.ParamEnum
import com.ctre.phoenix.motorcontrol.*
import com.ctre.phoenix.motorcontrol.can.TalonSRX
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser
import openrio.powerup.MatchData
import org.snakeskin.LightLink
import org.snakeskin.component.Gearbox
import org.snakeskin.component.TankDrivetrain
import org.team401.robot2018.PDP
import org.team401.robot2018.auto.steps.AutoStep
import org.team401.robot2018.constants.ConstantsBase
import org.team401.robot2018.subsystems.ShiftCommand

/*
 * 2018-Robot-Code - Created on 1/17/18
 * Author: Cameron Earle
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at project root
 */

/**
 * @author Cameron Earle
 * @version 1/17/18
 */
typealias StepAdder = (AutoStep) -> Unit

fun IMotorControllerEnhanced.configZeroPosOnReverseLimit(enable: Boolean, timeout: Int = 0) {
    configSetParameter(ParamEnum.eClearPosOnLimitR, if (enable) 1.0 else 0.0, 0, 0, timeout)
}

fun IMotorControllerEnhanced.configZeroPosOnForwardLimit(enable: Boolean, timeout: Int = 0) {
    configSetParameter(ParamEnum.eClearPosOnLimitF, if (enable) 1.0 else 0.0, 0, 0, timeout)
}

fun MatchData.OwnedSide.invert(): MatchData.OwnedSide {
    return when (this) {
        MatchData.OwnedSide.RIGHT -> MatchData.OwnedSide.LEFT
        MatchData.OwnedSide.LEFT -> MatchData.OwnedSide.RIGHT
        else -> MatchData.OwnedSide.UNKNOWN
    }
}

operator fun MatchData.OwnedSide.not() = this.invert()

fun IMotorControllerEnhanced.pidf(p: Double = 0.0, i: Double = 0.0, d: Double = 0.0, f: Double = 0.0, slot: Int = 0, timeout: Int = 0) {
    config_kP(slot, p, timeout)
    config_kI(slot, i, timeout)
    config_kD(slot, d, timeout)
    config_kF(slot, f, timeout)
}

fun IMotorControllerEnhanced.pidf(pidf: ConstantsBase.PIDF) {
    pidf(pidf.P, pidf.I, pidf.D, pidf.F)
}

fun IMotorControllerEnhanced.voltageCompensation(desiredOutput : Double, nominal: Double) {
    set(ControlMode.PercentOutput, desiredOutput * (nominal/ busVoltage))
}

fun TankDrivetrain.getCurrent() = Math.max(left.master.outputCurrent, right.master.outputCurrent)

fun Gearbox.getCurrent(vararg pdpIds: Int): Double {
    val amps = arrayListOf<Double>(master.outputCurrent)
    slaves.forEachIndexed {
        i, slave ->
        if (slave is TalonSRX) {
            amps.add(slave.outputCurrent)
        } else {
            amps.add(PDP.getCurrent(pdpIds[i]))
        }
    }

    return amps.average()
}

fun TankDrivetrain.shiftUpdate(state: ShiftCommand): Boolean {
    if (shifterState != state.state) {
        shift(state.state)
        println("Drivetrain Shifted: $state")
        return true
    }
    return false
}

fun TankDrivetrain.linkSensors() {
    val timeout = 20
    val localFeedbackDevice = FeedbackDevice.CTRE_MagEncoder_Relative

    left.master.configSelectedFeedbackSensor(localFeedbackDevice, TalonEnums.DISTANCE_PID, timeout)
    right.master.configRemoteFeedbackFilter(left.master.deviceID, RemoteSensorSource.TalonSRX_SelectedSensor, TalonEnums.REMOTE_O, timeout)
    right.master.configRemoteFeedbackFilter(imu.deviceID, RemoteSensorSource.GadgeteerPigeon_Yaw, TalonEnums.REMOTE_1, timeout)
    right.master.configSensorTerm(SensorTerm.Sum0, localFeedbackDevice, timeout)
    right.master.configSensorTerm(SensorTerm.Sum1, FeedbackDevice.RemoteSensor0, timeout)

    right.master.configSelectedFeedbackSensor(FeedbackDevice.SensorSum, TalonEnums.DISTANCE_PID, timeout)
    right.master.configSelectedFeedbackSensor(FeedbackDevice.RemoteSensor1, TalonEnums.HEADING_PID, timeout)

    right.master.configSelectedFeedbackCoefficient(.5, TalonEnums.DISTANCE_PID, timeout)
    right.master.configSelectedFeedbackCoefficient(3600.0/8192.0, TalonEnums.HEADING_PID, timeout)
}

fun TankDrivetrain.linkSides() {
    (left.master as TalonSRX).follow(right.master, FollowerType.AuxOutput1)
}

fun TankDrivetrain.unlinkSides() {
    right.master.set(ControlMode.PercentOutput, 0.0)
    left.master.set(ControlMode.PercentOutput, 0.0)
}

fun TankDrivetrain.unlinkSensors() {
    val timeout = 20

    left.master.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative, TalonEnums.DISTANCE_PID, timeout)
    right.master.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative, TalonEnums.DISTANCE_PID, timeout)
    left.master.configSelectedFeedbackCoefficient(1.0, TalonEnums.DISTANCE_PID, timeout)
    right.master.configSelectedFeedbackCoefficient(1.0, TalonEnums.DISTANCE_PID, timeout)
}

fun Gearbox.encoderMissing() = (master as TalonSRX).sensorCollection.pulseWidthRiseToRiseUs == 0

fun Number.withinTolerance(other: Number, tolerance: Number) = Math.abs(this.toDouble() - other.toDouble()) < tolerance.toDouble()