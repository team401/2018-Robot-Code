package org.team401.robot2018.auto.motionprofile

import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.NeutralMode
import org.team401.robot2018.auto.HeadingTracker
import org.team401.robot2018.auto.steps.AutoStep
import org.team401.robot2018.etc.withinTolerance
import org.team401.robot2018.subsystems.Drivetrain

class ZeroPoint(val heading: Double, val kF: Double, val kP: Double, val kI: Double, val kD: Double, val kIZone: Double, val tolerance: Double, val timeout: Double = 0.0): AutoStep() {
    private val left = Drivetrain.left.master
    private var right = Drivetrain.right.master
    private val imu = Drivetrain.imu
    private var imuData = DoubleArray(3)
    private var error = 0.0
    private var lastError = 0.0
    private var output = 0.0
    private var dt = 0.0
    private var counter = 0
    private var accum = 0.0
    private var startTime = 0.0

    override fun entry(currentTime: Double) {
        done = false
        HeadingTracker.configureImu()
        imuData[0] = 0.0
        imuData[1] = 0.0
        imuData[2] = 0.0
        error = 0.0
        lastError = 0.0
        output = 0.0
        dt = 0.0
        counter = 0
        accum = 0.0
        startTime = currentTime
    }

    override fun action(currentTime: Double, lastTime: Double) {
        if (timeout > 0.0 && currentTime - startTime >= timeout) done = true
        imu.getYawPitchRoll(imuData)
        dt = currentTime - lastTime
        error = heading - imuData[0]
        if (Math.abs(error) > kIZone) {
            accum = 0.0
        } else {
            accum += kI * error
        }
        output = kF * Math.signum(error) + (kP * error) + accum + (kD * ((error - lastError) / dt))
        lastError = error
        left.set(ControlMode.PercentOutput, -output)
        right.set(ControlMode.PercentOutput, output)
        if (error.withinTolerance(0.0, tolerance)) {
            counter++
        } else {
            counter = 0
        }
        println("err: $error  counter: $counter")
        if (counter >= 10) {
            done = true
        }
    }

    override fun exit(currentTime: Double) {
        left.set(ControlMode.PercentOutput, 0.0)
        right.set(ControlMode.PercentOutput, 0.0)
        HeadingTracker.finishedHeading(heading)
        println("hdg done")
    }
}