package org.team401.robot2018.etc

import com.ctre.phoenix.motorcontrol.IMotorControllerEnhanced
import org.team401.robot2018.constants.Constants

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
                          pitchDiameter: Double = 1.805,
                          ticksPerRev: Double = 4096.0) = (ticksPerRev * inches) / (Math.PI * pitchDiameter)

        fun feetToTicks(feet: Double,
                        pitchDiameter: Double = 1.805,
                        ticksPerRev: Double = 4096.0) = inchesToTicks(feet * 12.0, pitchDiameter, ticksPerRev)
    }

    object Drivetrain {
        /**
         * Converts an input inches to native units (ticks), based on the constants encoder ticks per rev and wheel diameter
         */
        fun inchesToNativeUnits(inches: Double) = (Constants.DrivetrainParameters.ENCODER_TICKS * inches) / (Math.PI * Constants.DrivetrainParameters.WHEEL_DIAMETER)

        /**
         * Converts an input inches per second to native units (ticks / 100ms), based on the constants encoder ticks per rev and wheel diameter
         */
        fun ipsToNativeUnits(ips: Double) = (Constants.DrivetrainParameters.ENCODER_TICKS * ips) / (10.0 * Math.PI * Constants.DrivetrainParameters.WHEEL_DIAMETER)
    }

    object UnitConversions {
        fun revolutionsToNativeUnits(rotations: Double, ticksPerRev: Double = 4096.0) = rotations * ticksPerRev

        fun nativeUnitsToRevolutions(native: Double, ticksPerRev: Double = 4096.0) = native / ticksPerRev
        fun nativeUnitsToRpm(native: Double, ticksPerRev: Double = 4096.0) = native * 600.0 / ticksPerRev

        fun degreesToCTREDumbUnit(degrees: Double) = degrees * 64.0 //Because that makes sense

        fun inchesToTicks(inches: Double, radius: Double, ticksPerRev: Double = 4096.0) = (ticksPerRev * inches) / (2 * Math.PI * radius)
    }

    fun averageCurrent(vararg motors: IMotorControllerEnhanced): Double = motors.map { it.outputCurrent }.average()
    fun limit180(degIn: Double): Double {
        var degOut = degIn
        while (degOut >= 180.0) degOut -= 360.0
        while (degOut < -180.0) degOut += 360.0
        return degOut
    }
}
