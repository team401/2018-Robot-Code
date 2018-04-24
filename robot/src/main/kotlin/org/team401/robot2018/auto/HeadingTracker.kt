package org.team401.robot2018.auto

import org.team401.robot2018.auto.motionprofile.MotionProfile
import org.team401.robot2018.etc.RobotMath
import org.team401.robot2018.subsystems.Drivetrain

object HeadingTracker {
    private val imu = Drivetrain.imu
    private var headingError = 0.0
    private const val TIMEOUT = 1000
    private val imuData = DoubleArray(3)

    fun reset() {
        headingError = 0.0
        imuData[0] = 0.0
        imuData[1] = 0.0
        imuData[2] = 0.0
    }

    fun configureImu() = imu.setYaw(
            RobotMath.UnitConversions.degreesToCTREDumbUnit(-headingError),
            TIMEOUT
    )

    fun finishedHeading(headingSetpoint: Double) {
        imu.getYawPitchRoll(imuData)
        headingError = headingSetpoint - imuData[0]
    }

    fun finishedProfile(profile: MotionProfile) = finishedHeading(profile.getLastHeading())
}