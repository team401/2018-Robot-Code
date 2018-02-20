package org.team401.robot2018.auto.motion

import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.IMotorControllerEnhanced
import com.ctre.phoenix.sensors.PigeonIMU
import org.team401.robot2018.etc.RobotMath
import org.team401.robot2018.etc.withinTolerance
import kotlin.math.max
import kotlin.math.sign

/*
 * 2018-Robot-Code - Created on 2/20/18
 * Author: Cameron Earle
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at project root
 */

/**
 * @author Cameron Earle
 * @version 2/20/18
 */

class GyroTurn(override val leftController: IMotorControllerEnhanced,
               override val rightController: IMotorControllerEnhanced,
               val imu: PigeonIMU,
               val targetDegrees: Double,
               val gain: Double,
               val feedForward: Double,
               val allowedError: Double,
               val maxOutput: Double = 1.0): TankMotionStep() {

    private val imuData = DoubleArray(3)
    private var output = 0.0
    private var error = 0.0

    override fun entry() {
        imu.setYaw(0.0, 0)

        imuData[0] = 0.0
        imuData[1] = 0.0
        imuData[2] = 0.0

        output = 0.0
        error = 0.0
    }

    override fun action() {
        imu.getYawPitchRoll(imuData)
        error = targetDegrees - imuData[0]

        if (!error.withinTolerance(0.0, allowedError)) {
            output = gain * error + (feedForward * Math.signum(error))
            if (output > maxOutput) output = maxOutput
            if (output < -maxOutput) output = -maxOutput

            leftController.set(ControlMode.PercentOutput, -output)
            rightController.set(ControlMode.PercentOutput, output)
        } else {
            done = true
        }
    }

    override fun exit() {
        leftController.set(ControlMode.PercentOutput, 0.0)
        rightController.set(ControlMode.PercentOutput, 0.0)
    }
}