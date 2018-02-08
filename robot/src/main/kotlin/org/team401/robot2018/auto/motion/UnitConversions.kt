package org.team401.robot2018.auto.motion

/*
 * 2018-Robot-Code - Created on 2/7/18
 * Author: Cameron Earle
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at project root
 */

/**
 * @author Cameron Earle
 * @version 2/7/18
 */

object UnitConversions {
    fun rotationsToNativeUnits(rotations: Double, ticksPerRev: Double = 4096.0) = rotations * ticksPerRev

    fun rpmToNativeUnits(rpm: Double, ticksPerRev: Double = 4096.0) = rpm * ticksPerRev / 600.0
    fun rpmpsToNativeUnits(rpmps: Double, ticksPerRev: Double = 4096.0) = rpmps //TODO
}