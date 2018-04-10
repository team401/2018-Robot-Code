package org.team401.robot2018.auto.motionprofile

import com.ctre.phoenix.motion.MotionProfileStatus
import com.ctre.phoenix.motion.SetValueMotionProfile
import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.can.TalonSRX
import org.snakeskin.component.TankDrivetrain
import org.team401.robot2018.auto.steps.AutoStep
import org.team401.robot2018.etc.linkSides

/*
 * 2018-Robot-Code - Created on 4/10/18
 * Author: Cameron Earle
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at project root
 */

/**
 * @author Cameron Earle
 * @version 4/10/18
 */

class ArcProfileFollower(val drivetrain: TankDrivetrain) : AutoStep() {
    private val controller = drivetrain.right.master as TalonSRX
    private var setValue = SetValueMotionProfile.Disable
    private val status = MotionProfileStatus()
    private val profile = MotionProfile()
    private var loadPromise: ProfileLoader.LoadPromise? = null
    private var streamIdx = 0

    fun load(filename: String) {
        loadPromise = ProfileLoader.populateLater(filename, profile)
    }

    enum class MpState {
        NOT_SETUP,
        STREAMING, //Awaiting number of points > min
        RUNNING, //In progress
        HOLDING //Done
    }

    private var mpState = MpState.NOT_SETUP

    private fun controllerSet() = controller.set(ControlMode.MotionProfileArc, setValue.value.toDouble())

    private fun resetAll() {
        setValue = SetValueMotionProfile.Disable
        mpState = MpState.NOT_SETUP
        streamIdx = 0
        controller.clearMotionProfileTrajectories()
        controller.clearMotionProfileHasUnderrun(0)
        controller.configMotionProfileTrajectoryPeriod(0, 0)
    }

    private fun streamPoints() {
        while (streamIdx < profile.numPoints() && !controller.isMotionProfileTopLevelBufferFull) {
            controller.pushMotionProfileTrajectory(profile.getPoint(streamIdx).toTrajectoryPoint(profile.isFirst(streamIdx), profile.isLast(streamIdx)))
            streamIdx++
        }
    }

    private fun checkMinPoints(minTimeRequired: Int): Boolean {
        val duration = profile.getPoint(0).timestep
        val numPoints = status.btmBufferCnt
        if (numPoints * duration > minTimeRequired) {
            return true
        }
        return false
    }

    private fun checkHold() = status.activePointValid && status.isLast

    override fun entry(currentTime: Double) {
        controllerSet()
        resetAll()
        drivetrain.linkSides()
        loadPromise?.await()
    }

    override fun action(currentTime: Double, lastTime: Double) {
        controller.getMotionProfileStatus(status)
        streamPoints()
        controller.processMotionProfileBuffer()
        when (mpState) {
            MpState.NOT_SETUP -> {
                setValue = SetValueMotionProfile.Invalid
                throw IllegalStateException("action called before entry!")
            }
            MpState.STREAMING -> {
                setValue = SetValueMotionProfile.Disable
                if (checkMinPoints(500)) {
                    //Require 500ms of points
                    mpState = MpState.RUNNING
                }
            }
            MpState.RUNNING -> {
                setValue = SetValueMotionProfile.Enable
                if (checkHold()) {
                    mpState = MpState.HOLDING
                }
            }
            MpState.HOLDING -> {
                setValue = SetValueMotionProfile.Hold
                done = true
            }
        }
        controllerSet()
    }

    override fun exit(currentTime: Double) {

    }
}