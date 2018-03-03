package org.team401.robot2018.etc

import com.ctre.phoenix.ParamEnum
import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.IMotorControllerEnhanced
import com.ctre.phoenix.motorcontrol.can.TalonSRX
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser
import openrio.powerup.MatchData
import org.snakeskin.component.Gearbox
import org.snakeskin.component.TankDrivetrain
import org.team401.robot2018.PDP
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

fun IMotorControllerEnhanced.pidf(p: Double = 0.0, i: Double = 0.0, d: Double = 0.0, f: Double = 0.0, slot: Int = 0, timeout: Int = 0) {
    config_kP(slot, p, timeout)
    config_kI(slot, i, timeout)
    config_kD(slot, d, timeout)
    config_kF(slot, f, timeout)
}

fun IMotorControllerEnhanced.pidf(pidf: Constants.PIDF) {
    pidf(pidf.P, pidf.I, pidf.D, pidf.F)
}

fun IMotorControllerEnhanced.voltageCompensation(desiredOutput : Double, nominal: Double) {
    set(ControlMode.PercentOutput, desiredOutput * (nominal/ PDP.voltage))
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

fun Double.withinTolerance(other: Double, tolerance: Double) = Math.abs(this - other) < tolerance