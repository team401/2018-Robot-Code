package org.team401.robot2018.auto

import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.IMotorControllerEnhanced
import org.team401.robot2018.Constants
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
class RioProfileRunner(val controller: IMotorControllerEnhanced, val rate: Long = 20L): AutoStep() {
    data class Point(val position: Double, val velocity: Double, val timestep: Int, val acceleration: Double)

    private fun genPoint(line: String): Point {
        val split = line.split(",")
        val position = split[0].toDouble()
        val velocity = split[1].toDouble()
        val duration = split[2].toInt()
        val acceleration = split[3].toDouble()

        return Point(position, velocity, duration, acceleration)
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
    var f = 0.0; private set
    var v = 0.0; private set

    fun setPIDFV(p: Double = 0.0, i: Double = 0.0, d: Double = 0.0, f: Double = 0.0, v: Double = 0.0) {
        this.p = p
        this.i = i
        this.d = d
        this.f = f
        this.v = v
    }

    private var lastError = 0.0
    private var point = Point(0.0, 0.0, 0, 0.0)
    private var error = 0.0
    private var output = 0.0
    private var reading = 0.0
    private var lastRun = 0L
    private var currentTime = 0L

    override fun entry() {
        pointIdx = 0
        lastError = 0.0
        point = Point(0.0, 0.0, 0, 0.0)
        error = 0.0
        output = 0.0
        reading = 0.0
        lastRun = 0L
    }

    override fun action() {
        currentTime = System.currentTimeMillis()
        if (currentTime - lastRun >= rate) {
            if (pointIdx < points.size) {
                point = points[pointIdx]
                reading = controller.getSelectedSensorPosition(0) / Constants.MotionProfileParameters.TICKS_PER_REV
                error = point.position - reading
                output = f + (p * error) + (d * (error - lastError) / point.timestep) + (v * point.velocity)
                lastError = error
                pointIdx++
            } else {
                output = 0.0
                done = true
            }
            lastRun = currentTime
            controller.set(ControlMode.PercentOutput, output)
        }
    }

    override fun exit() {
        controller.set(ControlMode.PercentOutput, 0.0)
    }
}