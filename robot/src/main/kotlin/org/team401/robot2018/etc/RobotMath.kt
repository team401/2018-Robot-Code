package org.team401.robot2018.etc

import com.ctre.phoenix.motorcontrol.IMotorControllerEnhanced

/*
 * 2018-Robot-Code - Created on 2/9/18
 * Author: Cameron Earle
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at project root
 */

/**
 * @author Cameron Earle
 * @version 2/9/18
 */

object RobotMath {
    object Elevator {
        fun inchesToTicks(inches: Double,
                          pitchDiameter: Double = Constants.ElevatorParameters.PITCH_DIAMETER,
                          ticksPerRev: Double = 4096.0) = (ticksPerRev * inches) / (Math.PI * pitchDiameter)

        fun feetToTicks(feet: Double,
                        pitchDiameter: Double = Constants.ElevatorParameters.PITCH_DIAMETER,
                        ticksPerRev: Double = 4096.0) = inchesToTicks(feet * 12.0, pitchDiameter, ticksPerRev)
    }

    object UnitConversions {
        fun revolutionsToNativeUnits(rotations: Double, ticksPerRev: Double = 4096.0) = rotations * ticksPerRev

        fun nativeUnitsToRevolutions(native: Double, ticksPerRev: Double = 4096.0) = native / ticksPerRev
        fun nativeUnitsToRpm(native: Double, ticksPerRev: Double = 4096.0) = native * 600.0 / ticksPerRev

        fun degreesToCTREDumbUnit(degrees: Double) = degrees * 64.0 //Because that makes sense
    }

    fun averageCurrent(vararg motors: IMotorControllerEnhanced): Double = motors.map { it.outputCurrent }.average()
}
