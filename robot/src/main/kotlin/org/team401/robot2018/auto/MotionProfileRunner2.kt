package org.team401.robot2018.auto

import com.ctre.phoenix.motion.MotionProfileStatus
import com.ctre.phoenix.motion.SetValueMotionProfile
import com.ctre.phoenix.motion.TrajectoryPoint
import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.IMotorControllerEnhanced
import org.snakeskin.factory.ExecutorFactory
import org.snakeskin.logic.LockingDelegate
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
    private var setValue by LockingDelegate(SetValueMotionProfile.Invalid)

    private var future: ScheduledFuture<*>? = null
    private val executor = ExecutorFactory.getExecutor("MPRunner2")


    private enum class MpState {
        NOT_SETUP,
        STREAMING,
        RUNNING,
        HOLDING,

    }
    private var mpState = MpState.NOT_SETUP

    /**
     * Resets the controller, clearing all bottom and top buffer trajectory points,
     * as well as clearing under-run status and putting the controller in profile mode
     */
    private fun resetController() {
        controller.set(ControlMode.MotionProfile, SetValueMotionProfile.Disable.value.toDouble())
        controller.clearMotionProfileTrajectories()
        do {
            controller.getMotionProfileStatus(status)
            Thread.sleep(5)
        } while (status.btmBufferCnt > 0 && status.topBufferCnt > 0)
        controller.clearMotionProfileHasUnderrun(0)
    }

    /**
     * Streams as many points as possible from our buffer into the top level buffer
     */
    private fun streamPoints() {
        while (streamIdx < points.size && !controller.isMotionProfileTopLevelBufferFull) {
            controller.pushMotionProfileTrajectory(points[streamIdx])
            streamIdx++
        }
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
        val timeDur = TrajectoryPoint.TrajectoryDuration.Trajectory_Duration_0ms.valueOf(duration / 2)

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

    override fun entry() {
        resetController() //Reset the controller to clear all buffers
        if (points.size > 0) {
            controller.changeMotionControlFramePeriod(points[0].timeDur.value / 2) //Set rate to half of per-point rate
        } else {
            controller.changeMotionControlFramePeriod(0) //Set rate to 0, as this profile is empty
        }
        streamPoints() //Stream as many points as possible to save time in the loop

        //Schedule the task to push points to and enable the controller, this should be done as fast as possible
        future = executor.scheduleAtFixedRate({ controller.processMotionProfileBuffer(); controller.set(ControlMode.MotionProfile, setValue.value.toDouble()) }, 0L, pushRate, TimeUnit.MILLISECONDS)
        mpState = MpState.STREAMING
    }

    override fun action() {
        controller.getMotionProfileStatus(status) //Fetch the status from the controller
        streamPoints() //Stream any remaining points to the controller, this should do nothing most of the time

        when (mpState) {
            //This state should only happen if the auto executor is calling things incorrectly
            MpState.NOT_SETUP -> {
                throw RuntimeException("'action()' called before 'entry()'!")
            }

            //Waiting for controller to hit min bottom buffer points
            MpState.STREAMING -> {
                setValue = SetValueMotionProfile.Disable
                if (status.btmBufferCnt >= Constants.MotionProfileParameters.MIN_POINTS) { //If we have enough points
                    mpState = MpState.RUNNING //Switch to the running state
                }
            }

            //Controller is enabled and executing the profile
            MpState.RUNNING -> {
                setValue = SetValueMotionProfile.Enable //Enable the controller
                if (status.activePointValid && status.isLast) { //If the profile is done
                    mpState = MpState.HOLDING //Switch to the holding state
                }
            }

            //Controller is holding the last point, this should happen exactly once
            MpState.HOLDING -> {
                setValue = SetValueMotionProfile.Hold //Hold the controller
                done = true //This AutoStep is now done
            }
        }
    }

    override fun exit() {
        future?.cancel(true) //Cancel the task responsible for streaming points
    }
}