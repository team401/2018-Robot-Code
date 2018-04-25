package org.team401.robot2018.auto

import org.team401.robot2018.auto.motionprofile.MotionProfile
import org.team401.robot2018.etc.RobotMath
import org.team401.robot2018.subsystems.Drivetrain

object HeadingTracker {
    private val imu = Drivetrain.imu
    private var lastSetpoint = 0.0
    private var headingError = 0.0
    private const val TIMEOUT = 1000
    private val imuData = DoubleArray(3)

    fun reset() {
        lastSetpoint = 0.0
        headingError = 0.0
        imuData[0] = 0.0
        imuData[1] = 0.0
        imuData[2] = 0.0
        imu.setYaw(0.0, TIMEOUT)
    }

    fun configureImu() {
        imu.getYawPitchRoll(imuData)
        headingError = lastSetpoint - imuData[0]
        imu.setYaw(
                RobotMath.UnitConversions.degreesToCTREDumbUnit(-headingError),
                TIMEOUT
        )
    }

    fun finishedHeading(headingSetpoint: Double) {
        lastSetpoint = headingSetpoint
    }

    fun finishedProfile(profile: MotionProfile) = finishedHeading(profile.getLastHeading())
}