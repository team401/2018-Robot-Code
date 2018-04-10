package org.team401.robot2018.auto.motionprofile

import java.io.File

/*
 * 2018-Robot-Code - Created on 4/9/18
 * Author: Cameron Earle
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at project root
 */

/**
 * @author Cameron Earle
 * @version 4/9/18
 */

/**
 * Converts output heading units of Pathfinder, which wrap around at the 0 border, to absolute heading units.
 * It accomplishes this by tracking the heading change across the current and last points.  If the delta is
 * 180 or greater (or the opposite), it will increment (or decrement) a rotation tracker and return a
 * new heading.
 */

class HeadingAdapter {
    private var numRotations = 0

    private fun track(lastAngle: Double, angle: Double): Double {
        if (angle - lastAngle > 180) {
            numRotations--
        } else if (angle - lastAngle < -180) {
            numRotations++
        }
        return (numRotations * 360.0) + angle
    }

    fun findNewHeadings(headings: List<Double>): List<Double> {
        val newHeadings = arrayListOf<Double>()
        for (i in 0 until headings.size) {
            val lastAngle = headings.getOrNull(i - 1) ?: headings[i]
            val angle = headings[i]
            newHeadings.add(track(lastAngle, angle))
        }
        return newHeadings
    }
}