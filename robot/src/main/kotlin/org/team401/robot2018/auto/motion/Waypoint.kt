package org.team401.robot2018.auto.motion

/*
 * 2018-Robot-Code - Created on 3/13/18
 * Author: Cameron Earle
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at project root
 */

/**
 * @author Cameron Earle
 * @version 3/13/18
 */
/**
 * Represents a single point in a profile
 */
data class Waypoint(val position: Double, val velocity: Double, val timestep: Int, val acceleration: Double, val heading: Double) {
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
            val acceleration = try {
                split[3].toDouble()
            } catch (e: Exception) {
                0.0
            }
            val heading = try {
                split[4].toDouble()
            } catch (e: Exception) {
                90.0
            }

            return Waypoint(position, velocity, timestep, acceleration, heading)
        }
    }
}