package org.team401.robot2018.auto

import com.ctre.phoenix.motorcontrol.IMotorControllerEnhanced
import org.team401.robot2018.auto.steps.AutoStep
import java.io.File

/*
 * 2018-Robot-Code - Created on 1/28/18
 * Author: Cameron Earle
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at project root
 */

/**
 * @author Cameron Earle
 * @version 1/28/18
 */

/**
 * RIO side motion profile runner.  Inspired by Jaci's DistanceFollower from Pathfinder
 * https://github.com/JacisNonsense/Pathfinder/blob/master/Pathfinder-Java/src/main/java/jaci/pathfinder/followers/DistanceFollower.java
 */
class RioProfileRunner(controller: IMotorControllerEnhanced): AutoStep() {
    data class Point(val position: Double, val velocity: Double, val timestep: Int, val acceleration: Double)

    private fun genPoint(line: String): Point {
        val split = line.split(",")
        val position = split[0].toDouble()
        val velocity = split[1].toDouble()
        val duration = split[2].toInt()
        val acceleration = split[3].toDouble()

        return Point(position, velocity, duration, acceleration)
    }

    private fun nativeToMs(nativeIn: Double): Double {
        return 0.0
    }

    private val points = arrayListOf<Point>()
    private var pointIdx = 0

    fun loadPoints(filename: String) {
        points.clear()

        val lines = File(filename).readLines()
        lines.forEach {
            points.add(genPoint(it))
        }

    }

    var p = 0.0; private set
    var i = 0.0; private set
    var d = 0.0; private set
    var v = 0.0; private set
    var a = 0.0; private set

    fun setPIDVA(p: Double = 0.0, i: Double = 0.0, d: Double = 0.0, v: Double = 0.0, a: Double = 0.0) {
        this.p = p
        this.i = i
        this.d = d
        this.v = v
        this.a = a
    }

    private var lastError = 0.0
    private var point = Point(0.0, 0.0, 0, 0.0)
    private var error = 0.0
    private var reading = 0.0

    override fun entry() {
        pointIdx = 0
        lastError = 0.0
    }

    override fun action() {
        if (pointIdx < points.size) {
            point = points[pointIdx]
            reading =
        }
    }

    override fun exit() {

    }
}