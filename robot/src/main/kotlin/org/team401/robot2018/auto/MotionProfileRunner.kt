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

class MotionProfileRunner(val controller: IMotorControllerEnhanced, val pushRate: Long = 5L): AutoStep() {
    private enum class MpState {
        WAIT,
        CHECK_ENABLE,
        CHECK_HOLD,
        DONE
    }

    val status = MotionProfileStatus()

    private val points = arrayListOf<TrajectoryPoint>()

    private var setValue = SetValueMotionProfile.Disable

    private var mpState = MpState.CHECK_ENABLE

    private val executor = ExecutorFactory.getExecutor("")
    private var future: ScheduledFuture<*>? = null

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

    private fun fill() {
        points.forEach {
            controller.pushMotionProfileTrajectory(it)
        }
    }

    fun load(fileName: String) {
        points.clear()
        val lines = File(fileName).readLines()
        lines.forEachIndexed {
            i, line ->
            val point = genPoint(line, i, lines.lastIndex)
            points.add(point)
        }
    }

    override fun entry() {
        mpState = MpState.WAIT
        setValue = SetValueMotionProfile.Disable
        controller.set(ControlMode.MotionProfile, setValue.value.toDouble())
        controller.clearMotionProfileTrajectories()
        controller.clearMotionProfileHasUnderrun(0)
        controller.changeMotionControlFramePeriod(0)
        fill()
        mpState = MpState.CHECK_ENABLE
        Thread.sleep(20)
        future = executor.scheduleAtFixedRate({controller.processMotionProfileBuffer()}, 0, pushRate, TimeUnit.MILLISECONDS)
    }

    override fun action() {
        controller.getMotionProfileStatus(status)

        when (mpState) {
            MpState.WAIT -> {
                throw RuntimeException("'entry()' was not called before calling 'action()'")
            }

            MpState.CHECK_ENABLE -> {
                if (status.btmBufferCnt > Constants.MotionProfileParameters.MIN_POINTS) {
                    setValue = SetValueMotionProfile.Enable
                    mpState = MpState.CHECK_HOLD
                }
            }

            MpState.CHECK_HOLD -> {
                if (status.activePointValid && status.isLast) {
                    setValue = SetValueMotionProfile.Hold
                    mpState = MpState.DONE
                }
            }

            MpState.DONE -> {}
        }

        controller.set(ControlMode.MotionProfile, setValue.value.toDouble())
    }

    override fun exit() {
        future?.cancel(false)
    }
}