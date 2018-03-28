package org.team401.robot2018.auto.motion

import org.team401.robot2018.etc.RobotMath

class HeadingController(val gains: DriveGains, val magnitude: Double = 1.0) {
    fun calculate(heading: Double, desiredHeading: Double) =
            Math.max(-magnitude, Math.min(magnitude, gains.H * RobotMath.limit180(desiredHeading - heading)))
}