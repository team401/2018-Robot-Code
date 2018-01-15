package org.team401.robot2018

import com.ctre.phoenix.motion.MotionProfileStatus
import com.ctre.phoenix.motion.SetValueMotionProfile
import com.ctre.phoenix.motion.TrajectoryPoint
import com.ctre.phoenix.motorcontrol.IMotorControllerEnhanced
import org.snakeskin.factory.ExecutorFactory
import java.io.File
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

/*
 * 2018-Robot-Code - Created on 1/13/18
 * Author: Cameron Earle
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at project root
 */

/**
 * @author Cameron Earle
 * @version 1/13/18
 */

class MotionProfileRunner(val left: IMotorControllerEnhanced, val right: IMotorControllerEnhanced, leftFile: String, rightFile: String) {
    private val leftStatus = MotionProfileStatus()
    private val rightStatus = MotionProfileStatus()

    private val leftPoints = arrayListOf<TrajectoryPoint>()
    private val rightPoints = arrayListOf<TrajectoryPoint>()

    private var leftSetValue = SetValueMotionProfile.Disable
    private var rightSetValue = SetValueMotionProfile.Disable

    private var state = 0

    val executor = ExecutorFactory.getExecutor("")
    private var leftFuture: ScheduledFuture<*>? = null
    private var rightFuture: ScheduledFuture<*>? = null

    private fun genPoint(line: String, index: Int, max: Int): TrajectoryPoint {
        val point = TrajectoryPoint()
        val split = line.split(",")

        val position = split[0].toDouble()
        val velocity = split[1].toDouble()

        point.position = position * Constants.MotionProfileParameters.TICKS_PER_REV
        point.velocity = velocity * Constants.MotionProfileParameters.TICKS_PER_REV / 600.0
        point.timeDur = TrajectoryPoint.TrajectoryDuration.Trajectory_Duration_0ms
        point.zeroPos = (index == 0)
        point.isLastPoint = (index == max)

        point.headingDeg = 0.0
        point.profileSlotSelect0 = 0
        point.profileSlotSelect1 = 0

        return point
    }

    private fun fill() {
        leftPoints.forEach {
            left.pushMotionProfileTrajectory(it)
        }

        rightPoints.forEach {
            right.pushMotionProfileTrajectory(it)
        }
    }

    init {
        val leftLines = File(leftFile).readLines()
        leftLines.forEachIndexed {
            i, line ->
            val point = genPoint(line, i, leftLines.lastIndex)
            leftPoints.add(point)
        }
        val rightLines = File(rightFile).readLines()
        rightLines.forEachIndexed {
            i, line ->
            val point = genPoint(line, i, rightLines.lastIndex)
            rightPoints.add(point)
        }
    }

    fun reset() {
        leftPoints.clear()
        rightPoints.clear()
        state = 0

        leftSetValue = SetValueMotionProfile.Disable
        rightSetValue = SetValueMotionProfile.Disable
        left.clearMotionProfileTrajectories()
        right.clearMotionProfileTrajectories()
    }

    fun start() {
        leftFuture = executor.scheduleAtFixedRate({left.processMotionProfileBuffer()}, 0, 5, TimeUnit.MILLISECONDS)
        rightFuture = executor.scheduleAtFixedRate({right.processMotionProfileBuffer()}, 0, 5, TimeUnit.MILLISECONDS)
        fill()
    }

    fun tick() {
        left.getMotionProfileStatus(leftStatus)
        right.getMotionProfileStatus(rightStatus)

        when (state) {
            0 -> {

            }

            1 -> {

            }

            2 -> {

            }
        }
    }

    fun stop() {
        leftFuture?.cancel(false)
        rightFuture?.cancel(false)
    }
}