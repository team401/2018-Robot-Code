package org.team401.robot2018.auto.motion

import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.IMotorControllerEnhanced
import com.ctre.phoenix.sensors.PigeonIMU
import org.team401.robot2018.etc.RobotMath
import org.team401.robot2018.etc.withinTolerance

/*
 * 2018-Robot-Code - Created on 2/22/18
 * Author: Cameron Earle
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at project root
 */

/**
 * @author Cameron Earle
 * @version 2/22/18
 */

class EncoderDrive(override val leftController: IMotorControllerEnhanced,
                   override val rightController: IMotorControllerEnhanced,
                   val imu: PigeonIMU,
                   val targetInches: Double,
                   val wheelRadius: Double,
                   val gain: Double,
                   val feedForward: Double,
                   val headingCorrection: Double,
                   val allowedError: Double): TankMotionStep() {

    private val imuData = DoubleArray(3)
    private var position = 0.0
    private var error = 0.0
    private val targetTicks = RobotMath.UnitConversions.inchesToTicks(targetInches, wheelRadius)


    override fun entry() {
        done = false
        leftController.setSelectedSensorPosition(0, 0, 1000)
        rightController.setSelectedSensorPosition(0, 0, 1000)
        imu.setYaw(0.0, 1000)
        Thread.sleep(100)

        imuData[0] = 0.0
        imuData[1] = 0.0
        imuData[2] = 0.0

        position = 0.0

    }

    override fun action() {
        imu.getYawPitchRoll(imuData)
        position = (leftController.getSelectedSensorPosition(0) + rightController.getSelectedSensorPosition(0)) / 2.0
        error = targetTicks - position

        if (!error.withinTolerance(0.0, allowedError)) {
            leftController.set(ControlMode.PercentOutput, ((1 - position / targetTicks) * gain) + feedForward + (imuData[0] * headingCorrection))
            rightController.set(ControlMode.PercentOutput, ((1 - position / targetTicks) * gain) + feedForward - (imuData[0] * headingCorrection))
        } else {
            leftController.set(ControlMode.PercentOutput, 0.0)
            rightController.set(ControlMode.PercentOutput, 0.0)
            done = true
        }
    }

    override fun exit() {
        leftController.set(ControlMode.PercentOutput, 0.0)
        rightController.set(ControlMode.PercentOutput, 0.0)
    }
}