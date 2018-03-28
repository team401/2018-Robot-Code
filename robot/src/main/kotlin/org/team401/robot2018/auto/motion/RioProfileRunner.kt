package org.team401.robot2018.auto.motion

import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.IMotorControllerEnhanced
import com.ctre.phoenix.motorcontrol.can.TalonSRX
import com.ctre.phoenix.sensors.PigeonIMU
import org.snakeskin.component.TankDrivetrain
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

class RioProfileRunner(drivetrain: TankDrivetrain,
                       val gains: DriveGains,
                       val headingMagnitude: Double = 1.0,
                       val driveMagnitude: Double = 1.0,
                       val rate: Long = 20): TankMotionStep() {
    override val leftController = drivetrain.left.master
    override val rightController = drivetrain.right.master
    val imu = drivetrain.imu

    /**
     * Represents one side of the drivetrain.
     * Contains all of the points, controllers, and gains for that side.
     */
    private inner class MpSide(val controller: IMotorControllerEnhanced, val polarity: Double) {
        private val points = arrayListOf<Waypoint>()
        private var promise: ProfileLoader.LoadPromise? = null
        private val driveController = DriveController(gains, driveMagnitude)
        fun awaitLoading() = promise?.await()

        fun loadPoints(profile: String) {
            promise = ProfileLoader.populateLater(profile, points)
        }

        var currentWaypoint = Waypoint(0.0, 0.0, 0, 0.0, 0.0)
        var done = false
        var position = 0.0
        var velocity = 0.0
        var value = 0.0

        fun reset() {
            controller.set(ControlMode.PercentOutput, 0.0)
            position = 0.0
            velocity = 0.0
            value = 0.0
            currentWaypoint = Waypoint(0.0, 0.0, 0, 0.0, 0.0)
            done = false
        }

        fun numPoints() = points.size

        fun activeHeading() = currentWaypoint.heading

        fun calculate(index: Int) {
            currentWaypoint = points[index]
            position = RobotMath.UnitConversions.nativeUnitsToRevolutions(controller.getSelectedSensorPosition(0).toDouble())
            velocity = RobotMath.UnitConversions.nativeUnitsToRpm(controller.getSelectedSensorVelocity(0).toDouble())

            value = driveController.calculate(
                    position,
                    velocity,
                    currentWaypoint.position,
                    currentWaypoint.velocity,
                    currentWaypoint.acceleration
            )
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

    private val left = MpSide(leftController, -1.0)
    private val right = MpSide(rightController, 1.0)

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
    private var headingError = 0.0
    private var lastHeadingError = 0.0
    private var headingAdjustment = 0.0
    private val imuValue = DoubleArray(3)
    private val headingController = HeadingController(gains, headingMagnitude)

    override fun entry() {
        done = false
        left.reset()
        right.reset()

        lastUpdate = 0L
        currentTime = 0L
        pointIdx = 0
        headingError = 0.0
        headingAdjustment = 0.0

        imuValue[0] = 0.0
        imuValue[1] = 0.0
        imuValue[2] = 0.0

        left.zero(0)
        right.zero(0)

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
        headingAdjustment = headingController.calculate(imuValue[0], left.activeHeading())

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