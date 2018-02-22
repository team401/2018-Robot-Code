package org.team401.robot2018.auto.motion

import com.ctre.phoenix.motion.MotionProfileStatus
import com.ctre.phoenix.motion.SetValueMotionProfile
import com.ctre.phoenix.motion.TrajectoryPoint
import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.can.TalonSRX
import org.snakeskin.factory.ExecutorFactory
import org.team401.robot2018.etc.Constants
import java.io.File
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

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

class MotionProfileRunner(override val leftController: TalonSRX, override val rightController: TalonSRX, val pushRate: Long = 5L): TankMotionStep() {
    private enum class MpState {
        NOT_SETUP,
        STREAMING,
        RUNNING,
        HOLDING,
    }

    private var mpState = MpState.NOT_SETUP

    private inner class MpSide(val controller: TalonSRX) {
        val points = arrayListOf<TrajectoryPoint>()
        var streamIdx = 0
        val status = MotionProfileStatus()

        /**
         * Streams as many points as possible from our buffer into the top level buffer
         */
        fun streamPoints() {
            while (streamIdx < points.size && !controller.isMotionProfileTopLevelBufferFull) {
                controller.pushMotionProfileTrajectory(points[streamIdx])
                streamIdx++
            }
        }

        fun resetController() {
            streamIdx = 0

            controller.set(ControlMode.MotionProfile, SetValueMotionProfile.Disable.value.toDouble())
            controller.clearMotionProfileTrajectories()
            do {
                controller.getMotionProfileStatus(status)
                Thread.sleep(5)
            } while (status.btmBufferCnt > 0 && status.topBufferCnt > 0)
            controller.clearMotionProfileHasUnderrun(0)

            controller.changeMotionControlFramePeriod(5)
            controller.configMotionProfileTrajectoryPeriod(0, 0)
        }

        /**
         * Loads points into the runner from the given file path
         *
         * @param filename The absolute path of the file to load from
         */
        fun loadPoints(filename: String) {
            points.clear()

            val lines = File(filename).readLines()
            lines.forEachIndexed {
                i, line ->
                val point = genPoint(line, i, lines.lastIndex)
                points.add(point)
            }
        }

        fun loadStatus() {
            controller.getMotionProfileStatus(status)
        }

        fun checkMinPoints() = status.btmBufferCnt >= Constants.MotionProfileParameters.MIN_POINTS
        fun checkHold() = status.activePointValid && status.isLast
    }

    private val left: MpSide
    private val right: MpSide

    init {
        left = MpSide(leftController)
        right = MpSide(rightController)
    }

    /**
     * Generates a trajectory point from an appropriate csv line
     *
     * @param line The line from the csv file (position,velocity,duration)
     * @param index The current line of the file being read
     * @param max The index of the last line in the file
     * @return The generated trajectory point
     */
    private fun genPoint(line: String, index: Int, max: Int): TrajectoryPoint {
        val point = TrajectoryPoint()
        val split = line.split(",") //Read in the line as an array

        val position = split[0].toDouble()
        val velocity = split[1].toDouble()
        val duration = split[2].toInt()

        //Calculate the individual time duration by dividing the given duration in half
        //The other half is handled in the "motion control rate" setting on the controller
        val timeDur = TrajectoryPoint.TrajectoryDuration.Trajectory_Duration_0ms.valueOf(duration)

        point.position = position * Constants.MotionProfileParameters.TICKS_PER_REV //revolutions to ticks
        point.velocity = velocity * Constants.MotionProfileParameters.TICKS_PER_REV / 600.0 //rpm to ticks per 100 ms
        point.timeDur = timeDur
        point.zeroPos = (index == 0) //Is this the first line?
        point.isLastPoint = (index == max) //Is this the last line?

        //Unused features by CTRE
        point.headingDeg = 0.0
        point.profileSlotSelect0 = 0
        point.profileSlotSelect1 = 0

        return point
    }

    fun loadPoints(leftPoints: String, rightPoints: String) {
        left.loadPoints(leftPoints)
        right.loadPoints(rightPoints)
    }

    private fun setValue(value: SetValueMotionProfile) {
        left.controller.set(ControlMode.MotionProfile, value.value.toDouble())
        right.controller.set(ControlMode.MotionProfile, value.value.toDouble())
    }

    private val executor = ExecutorFactory.getExecutor("MPRunner3")
    private var future: ScheduledFuture<*>? = null

    private fun pushPoints() {
        left.controller.processMotionProfileBuffer()
        right.controller.processMotionProfileBuffer()
    }

    override fun entry() {
        left.resetController()
        right.resetController()

        left.streamPoints()
        right.streamPoints()

        future = executor.scheduleAtFixedRate(::pushPoints, 0L, pushRate, TimeUnit.MILLISECONDS)

        mpState = MpState.STREAMING
    }

    override fun action() {
        left.loadStatus()
        right.loadStatus()

        left.streamPoints()
        right.streamPoints()

        when (mpState) {
            //This state should only happen if the auto executor is calling things incorrectly
            MpState.NOT_SETUP -> {
                setValue(SetValueMotionProfile.Invalid)
                throw RuntimeException("'action()' called before 'entry()'!")
            }

            //Waiting for controller to hit min bottom buffer points
            MpState.STREAMING -> {
                setValue(SetValueMotionProfile.Disable)
                if (left.checkMinPoints() && right.checkMinPoints()) { //If we have enough points
                    mpState = MpState.RUNNING //Switch to the running state
                }
            }

            //Controller is enabled and executing the profile
            MpState.RUNNING -> {
                setValue(SetValueMotionProfile.Enable) //Enable the controller
                if (left.checkHold() && right.checkHold()) { //If the profile is done
                    mpState = MpState.HOLDING //Switch to the holding state
                }
            }

            //Controller is holding the last point, this should happen exactly once
            MpState.HOLDING -> {
                setValue(SetValueMotionProfile.Hold) //Hold the controller
                done = true //This AutoStep is now done
            }
        }
    }

    override fun exit() {
        future?.cancel(true)
        future = null
    }
}