package org.team401.robot2018.auto.motion

import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.IMotorControllerEnhanced
import com.ctre.phoenix.sensors.PigeonIMU
import org.team401.robot2018.etc.RobotMath

/*
 * 2018-Robot-Code - Created on 2/6/18
 * Author: Cameron Earle
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at project root
 */

/**
 * @author Cameron Earle
 * @version 2/6/18
 */

class RioProfileRunner(override val leftController: IMotorControllerEnhanced, override val rightController: IMotorControllerEnhanced, val imu: PigeonIMU, val leftGains: PDVA, val rightGains: PDVA, val headingGain: Double = 0.0, val rate: Long = 20): TankMotionStep() {
    /**
     * Represents one side of the drivetrain.
     * Contains all of the points, controllers, and gains for that side.
     */
    private inner class MpSide(val controller: IMotorControllerEnhanced, val gains: PDVA, val polarity: Double) {
        private val points = arrayListOf<Waypoint>()
        private var promise: ProfileLoader.LoadPromise? = null
        fun awaitLoading() = promise?.await()

        fun loadPoints(profile: String) {
            promise = ProfileLoader.populateLater(profile, points)
        }

        var error = 0.0
        var lastError = 0.0
        var sensor = 0.0
        var value = 0.0
        var currentWaypoint = Waypoint(0.0, 0.0, 0, 0.0, 0.0)
        var saturated = false
        var done = false

        fun reset() {
            controller.set(ControlMode.PercentOutput, 0.0)

            error = 0.0
            lastError = 0.0
            sensor = 0.0
            value = 0.0
            currentWaypoint = Waypoint(0.0, 0.0, 0, 0.0, 0.0)
            saturated = false
            done = false
        }

        fun numPoints() = points.size

        fun activeHeading() = currentWaypoint.heading

        fun calculate(index: Int): Boolean {
            saturated = false
            currentWaypoint = points[index]
            sensor = RobotMath.UnitConversions.nativeUnitsToRevolutions(controller.getSelectedSensorPosition(0).toDouble())

            error = currentWaypoint.position - sensor
            value =
                    gains.p * error +
                    gains.d * ((error - lastError) / currentWaypoint.timestep) +
                    (gains.v * currentWaypoint.velocity + gains.a * currentWaypoint.acceleration)

            if (value > 1.0) {
                value = 1.0
                saturated = true
            }
            if (value < -1.0) {
                value = -1.0
                saturated = true
            }
            lastError = error
            return saturated
        }

        fun done() {
            value = 0.0
            done = true
        }

        fun updateController(headingCorrection: Double) {
            if (!done) {
                controller.set(ControlMode.PercentOutput, value + (polarity * headingCorrection))
            } else {
                controller.set(ControlMode.PercentOutput, 0.0)
            }
        }

        fun zero(position: Int) {
            controller.setSelectedSensorPosition(position, 0, 0)
        }
    }

    private val left = MpSide(leftController, leftGains, -1.0)
    private val right = MpSide(rightController, rightGains, 1.0)

    fun leftCurrentWaypoint() = left.currentWaypoint
    fun rightCurrentWaypoint() = right.currentWaypoint
    fun index() = pointIdx

    fun loadPoints(leftFilename: String, rightFilename: String) {
        left.loadPoints(leftFilename)
        right.loadPoints(rightFilename)
    }

    private var lastUpdate = 0L
    private var currentTime = 0L
    private var pointIdx = 0
    private var headingAdjustment = 0.0
    private var desiredHeading = 0.0
    private val imuValue = DoubleArray(3)

    override fun entry() {
        done = false
        left.reset()
        right.reset()

        lastUpdate = 0L
        currentTime = 0L
        pointIdx = 0
        headingAdjustment = 0.0
        desiredHeading = 0.0

        imuValue[0] = 0.0
        imuValue[1] = 0.0
        imuValue[2] = 0.0

        imu.getYawPitchRoll(imuValue)
        imu.setYaw(RobotMath.UnitConversions.degreesToCTREDumbUnit(imuValue[0] % 360), 100)
        left.zero(0)
        right.zero(0)
        Thread.sleep(100)

        left.awaitLoading() //Wait for points to finish loading
        right.awaitLoading()
    }

    override fun action() {
        currentTime = System.currentTimeMillis()

        if (pointIdx < Math.min(left.numPoints(), right.numPoints())) {
            left.calculate(pointIdx)
            right.calculate(pointIdx)
        } else {
            left.done()
            right.done()
            done = true
        }

        imu.getYawPitchRoll(imuValue)
        desiredHeading = left.activeHeading()

        headingAdjustment = headingGain * (desiredHeading - imuValue[0])

        left.updateController(headingAdjustment)
        right.updateController(headingAdjustment)

        if (currentTime - lastUpdate >= rate) {
            pointIdx++
            lastUpdate = currentTime
        }
    }

    override fun exit() {
        left.controller.set(ControlMode.PercentOutput, 0.0)
        right.controller.set(ControlMode.PercentOutput, 0.0)
    }
}