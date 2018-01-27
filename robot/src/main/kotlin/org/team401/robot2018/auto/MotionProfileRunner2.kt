package org.team401.robot2018.auto

import com.ctre.phoenix.motion.MotionProfileStatus
import com.ctre.phoenix.motion.SetValueMotionProfile
import com.ctre.phoenix.motion.TrajectoryPoint
import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.IMotorControllerEnhanced
import org.snakeskin.factory.ExecutorFactory
import org.team401.robot2018.Constants
import org.team401.robot2018.auto.steps.AutoStep
import java.io.File
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

/*
 * 2018-Robot-Code - Created on 1/26/18
 * Author: Cameron Earle
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at project root
 */

/**
 * @author Cameron Earle
 * @version 1/26/18
 */

class MotionProfileRunner2(val controller: IMotorControllerEnhanced, val pushRate: Long = 5L): AutoStep() {
    private val points = arrayListOf<TrajectoryPoint>()
    private var streamIdx = 0
    private val status = MotionProfileStatus()
    private var setValue = SetValueMotionProfile.Invalid

    private val executor = ExecutorFactory.getExecutor("MPRunner")
    private var future: ScheduledFuture<*>? = null

    private enum class MpState {
        NOT_SETUP,
        STREAMING,
        RUNNING,
        HOLDING,

    }
    private var mpState = MpState.NOT_SETUP

    private fun resetController() {
        controller.set(ControlMode.MotionProfile, SetValueMotionProfile.Disable.value.toDouble())
        controller.clearMotionProfileTrajectories()
        do {
            controller.getMotionProfileStatus(status)
            Thread.sleep(1)
        } while (status.btmBufferCnt > 0)
        controller.clearMotionProfileHasUnderrun(0)
        controller.changeMotionControlFramePeriod(0)
    }

    private fun streamPoints() {
        while (streamIdx < points.size && !controller.isMotionProfileTopLevelBufferFull) {
            controller.pushMotionProfileTrajectory(points[streamIdx])
            streamIdx++
        }
    }

    private fun genPoint(line: String, index: Int, max: Int): TrajectoryPoint {
        val point = TrajectoryPoint()
        val split = line.split(",")

        val position = split[0].toDouble()
        val velocity = split[1].toDouble()
        val duration = split[2].toInt()

        val timeDur = TrajectoryPoint.TrajectoryDuration.Trajectory_Duration_0ms.valueOf(duration)

        point.position = position * Constants.MotionProfileParameters.TICKS_PER_REV
        point.velocity = velocity * Constants.MotionProfileParameters.TICKS_PER_REV / 600.0
        point.timeDur = timeDur
        point.zeroPos = (index == 0)
        point.isLastPoint = (index == max)

        point.headingDeg = 0.0
        point.profileSlotSelect0 = 0
        point.profileSlotSelect1 = 0

        return point
    }

    fun loadPoints(filename: String) {
        points.clear()

        val lines = File(filename).readLines()
        lines.forEachIndexed {
            i, line ->
            val point = genPoint(line, i, lines.lastIndex)
            points.add(point)
        }
    }

    override fun entry() {
        resetController()
        future = executor.scheduleAtFixedRate({ controller.processMotionProfileBuffer() }, 0L, pushRate, TimeUnit.MILLISECONDS)
        mpState = MpState.STREAMING
    }

    override fun action() {
        controller.getMotionProfileStatus(status)
        streamPoints()

        when (mpState) {
            MpState.NOT_SETUP -> {
                setValue = SetValueMotionProfile.Invalid
                throw RuntimeException("'action()' called before 'entry()'!")
            }

            MpState.STREAMING -> {
                setValue = SetValueMotionProfile.Disable
                if (status.btmBufferCnt >= Constants.MotionProfileParameters.MIN_POINTS) {
                    mpState = MpState.RUNNING
                }
            }

            MpState.RUNNING -> {
                setValue = SetValueMotionProfile.Enable
                if (status.activePointValid && status.isLast) {
                    mpState = MpState.HOLDING
                }
            }

            MpState.HOLDING -> {
                setValue = SetValueMotionProfile.Hold
                done = true
            }
        }

        controller.set(ControlMode.MotionProfile, setValue.value.toDouble())
    }

    override fun exit() {
        future?.cancel(false)
    }
}