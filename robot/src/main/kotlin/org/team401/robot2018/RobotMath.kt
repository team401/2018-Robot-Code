package org.team401.robot2018

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
}