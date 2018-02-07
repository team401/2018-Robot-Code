package org.team401.robot2018.auto.motion

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

class RioProfileRunner(override val leftController: IMotorControllerEnhanced, override val rightController: IMotorControllerEnhanced, val imu: PigeonIMU, val leftGains: PIDFVAH, val rightGains: PIDFVAH): TankMotionStep() {
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
                val position = split[0].toDouble()
                val velocity = split[1].toDouble()
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
    private inner class MpSide(val controller: IMotorControllerEnhanced, val gains: PIDFVAH, val polarity: Double) {
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

        fun reset() {
            error = 0.0
            lastError = 0.0
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

    override fun entry() {
        left.reset()
        right.reset()
    }

    override fun action() {

    }

    override fun exit() {

    }
}