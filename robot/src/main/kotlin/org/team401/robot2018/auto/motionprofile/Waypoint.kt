package org.team401.robot2018.auto.motionprofile

import com.ctre.phoenix.motion.TrajectoryPoint
import org.team401.robot2018.etc.RobotMath
import org.team401.robot2018.etc.TalonEnums

/*
 * 2018-Robot-Code - Created on 4/9/18
 * Author: Cameron Earle
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at project root
 */

/**
 * @author Cameron Earle
 * @version 4/9/18
 */

data class Waypoint(var position: Double,
                    var velocity: Double,
                    var timestep: Int,
                    var acceleration: Double,
                    var heading: Double) {

    fun toTrajectoryPoint(isFirst: Boolean, isLast: Boolean): TrajectoryPoint {
        val tp = TrajectoryPoint()
        tp.position = RobotMath.Drivetrain.inchesToNativeUnits(position)
        tp.velocity = RobotMath.Drivetrain.ipsToNativeUnits(velocity)
        tp.auxiliaryPos = heading * 10.0
        tp.headingDeg = heading
        tp.timeDur = TrajectoryPoint.TrajectoryDuration.Trajectory_Duration_0ms.valueOf(timestep)
        tp.profileSlotSelect0 = 0
        tp.profileSlotSelect1 = 1
        tp.zeroPos = isFirst
        tp.isLastPoint = isLast
        return tp
    }

    companion object {
        fun fromCSVLine(line: String): Waypoint {
            val split = line.split(',')
            val position = (split.getOrNull(0) ?: "0.0").toDoubleOrNull() ?: 0.0
            val velocity = (split.getOrNull(1) ?: "0.0").toDoubleOrNull() ?: 0.0
            val timestep = (split.getOrNull(2) ?: "0").toIntOrNull() ?: 0
            val acceleration = (split.getOrNull(3) ?: "0.0").toDoubleOrNull() ?: 0.0
            val heading = (split.getOrNull(4) ?: "90.0").toDoubleOrNull() ?: 90.0
            return Waypoint(position, velocity, timestep, acceleration, heading)
        }
    }
}