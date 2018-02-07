package org.team401.robot2018.auto.motion

import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.IMotorControllerEnhanced
import com.ctre.phoenix.sensors.PigeonIMU
import java.io.File

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

class RioProfileRunner(override val leftController: IMotorControllerEnhanced, override val rightController: IMotorControllerEnhanced, val imu: PigeonIMU, val leftGains: PDFVA, val rightGains: PDFVA, val headingGain: Double = 0.0, val rate: Long = 20): TankMotionStep() {
    /**
     * Represents a single point in a profile
     */
    private data class Waypoint(val position: Double, val velocity: Double, val timestep: Int, val acceleration: Double, val heading: Double) {
        companion object {
            /**
             * Generates a Waypoint from an input CSV line
             * @param line The CSV line
             * @return The Waypoint
             */
            fun fromCsv(line: String): Waypoint {
                val split = line.split(",")
                val position = split[0].toDouble() * 4096
                val velocity = split[1].toDouble() * 4096 / 600
                val timestep = split[2].toInt()
                val acceleration = split[3].toDouble()
                val heading = split[4].toDouble()

                return Waypoint(position, velocity, timestep, acceleration, heading)
            }
        }
    }

    /**
     * Represents one side of the drivetrain.
     * Contains all of the points, controllers, and gains for that side.
     */
    private inner class MpSide(val controller: IMotorControllerEnhanced, val gains: PDFVA, val polarity: Double) {
        private val points = arrayListOf<Waypoint>()

        fun loadPoints(file: File) {
            points.clear()

            val lines = file.readLines()
            lines.forEach {
                points.add(Waypoint.fromCsv(it))
            }
        }

        var error = 0.0
        var lastError = 0.0
        var sensor = 0.0
        var value = 0.0
        var currentWaypoint = Waypoint(0.0, 0.0, 0, 0.0, 0.0)

        fun reset() {
            error = 0.0
            lastError = 0.0
            sensor = 0.0
            value = 0.0
        }

        fun numPoints() = points.size

        fun activeHeading() = currentWaypoint.heading

        fun calculate(index: Int) {
            currentWaypoint = points[index]
            sensor = controller.getSelectedSensorPosition(0).toDouble()

            error = currentWaypoint.position - sensor
            value =
                    gains.f +
                    gains.p * error +
                    gains.d * ((error - lastError) / currentWaypoint.timestep) +
                    (gains.v * currentWaypoint.velocity + gains.a * currentWaypoint.velocity)

            lastError = error
        }
        fun done() {
            value = 0.0
        }

        fun updateController(headingCorrection: Double) {
            controller.set(ControlMode.PercentOutput, value + (polarity * headingCorrection))
        }
    }

    private val left = MpSide(leftController, leftGains, 1.0)
    private val right = MpSide(rightController, rightGains, -1.0)

    fun loadPoints(leftFilename: String, rightFilename: String) {
        val leftFile = File(leftFilename)
        val rightFile = File(rightFilename)
        left.loadPoints(leftFile)
        right.loadPoints(rightFile)
    }

    private var lastUpdate = 0L
    private var currentTime = 0L
    private var pointIdx = 0
    private var headingAdjustment = 0.0

    override fun entry() {
        left.reset()
        right.reset()

        lastUpdate = 0L
        currentTime = 0L
        pointIdx = 0
        headingAdjustment = 0.0
    }

    override fun action() {
        currentTime = System.currentTimeMillis()

        if (currentTime - lastUpdate >= rate) {
            pointIdx++
            lastUpdate = currentTime
        }

        if (pointIdx < Math.min(left.numPoints(), right.numPoints())) {
            left.calculate(pointIdx)
            right.calculate(pointIdx)
        } else {
            left.done()
            right.done()
            done = true
        }

        left.updateController()
        right.updateController()
    }

    override fun exit() {

    }
}