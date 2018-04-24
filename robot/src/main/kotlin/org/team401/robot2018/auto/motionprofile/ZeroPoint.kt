package org.team401.robot2018.auto.motionprofile

import com.ctre.phoenix.motorcontrol.ControlMode
import org.team401.robot2018.auto.HeadingTracker
import org.team401.robot2018.auto.steps.AutoStep
import org.team401.robot2018.etc.withinTolerance
import org.team401.robot2018.subsystems.Drivetrain

class ZeroPoint(val heading: Double, val kF: Double, val kP: Double, val kD: Double, val tolerance: Double): AutoStep() {
    private val left = Drivetrain.left.master
    private var right = Drivetrain.right.master
    private val imu = Drivetrain.imu
    private var imuData = DoubleArray(3)
    private var error = 0.0
    private var lastError = 0.0
    private var output = 0.0
    private var dt = 0.0
    private var counter = 0

    override fun entry(currentTime: Double) {
        HeadingTracker.configureImu()
        imuData[0] = 0.0
        imuData[1] = 0.0
        imuData[2] = 0.0
        error = 0.0
        lastError = 0.0
        output = 0.0
        dt = 0.0
        counter = 0
    }

    override fun action(currentTime: Double, lastTime: Double) {
        imu.getYawPitchRoll(imuData)
        dt = currentTime - lastTime
        error = heading - imuData[0]
        output = kF * Math.signum(error) + (kP * error + kD * ((error - lastError) / dt))
        lastError = error
        left.set(ControlMode.PercentOutput, -output)
        right.set(ControlMode.PercentOutput, output)
        if (error.withinTolerance(0.0, tolerance)) counter++
        else counter = 0
        done = counter >= 10
    }

    override fun exit(currentTime: Double) {
        left.set(ControlMode.PercentOutput, 0.0)
        right.set(ControlMode.PercentOutput, 0.0)
        HeadingTracker.finishedHeading(heading)
    }
}