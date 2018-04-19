package org.team401.robot2018.auto.motionprofile

import com.ctre.phoenix.motion.MotionProfileStatus
import com.ctre.phoenix.motion.SetValueMotionProfile
import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.FollowerType
import com.ctre.phoenix.motorcontrol.can.TalonSRX
import org.snakeskin.component.TankDrivetrain
import org.snakeskin.factory.ExecutorFactory
import org.team401.robot2018.auto.steps.AutoStep
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

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
    private var setValue = SetValueMotionProfile.Disable
    private val profile = MotionProfile()
    private var loadPromise: ProfileLoader.LoadPromise? = null
    private var streamIdx = 0

    val controller = drivetrain.right.master as TalonSRX
    val status = MotionProfileStatus()

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

    private fun streamPoints() {
        while (!done && streamIdx < profile.numPoints() && !controller.isMotionProfileTopLevelBufferFull) {
            controller.pushMotionProfileTrajectory(profile.getPoint(streamIdx).toTrajectoryPoint(profile.isFirst(streamIdx), profile.isLast(streamIdx)))
            streamIdx++
        }
    }

    private fun checkHold() = status.activePointValid && status.isLast

    override fun entry(currentTime: Double) {
        drivetrain.tank(ControlMode.PercentOutput, 0.0, 0.0)
        controllerSet()
        setValue = SetValueMotionProfile.Disable
        mpState = MpState.NOT_SETUP
        streamIdx = 0
        controller.clearMotionProfileTrajectories()
        controller.clearMotionProfileHasUnderrun(0)
        controller.changeMotionControlFramePeriod(5)
        controller.configMotionProfileTrajectoryPeriod(0, 0)
        loadPromise?.await()
        (drivetrain.left.master as TalonSRX).follow(controller, FollowerType.AuxOutput1)
        mpState = MpState.STREAMING
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
                println("STREAMING POINTS TO BOTTOM BUFFER: ${status.btmBufferCnt}")
                if (status.btmBufferCnt > 5) {
                    //Require 500ms of points
                    mpState = MpState.RUNNING
                }
            }
            MpState.RUNNING -> {
                setValue = SetValueMotionProfile.Enable
                println("RUNNING PROFILE.  DT: ${(currentTime - lastTime) * 1000}")
                if (checkHold()) {
                    mpState = MpState.HOLDING
                }
            }
            MpState.HOLDING -> {
                println("PROFILE DONE: HOLDING")
                setValue = SetValueMotionProfile.Hold
                done = true
            }
        }
        controllerSet()
        println("RAN")
    }

    override fun exit(currentTime: Double) {
        drivetrain.left.master.set(ControlMode.PercentOutput, 0.0)
        drivetrain.right.master.set(ControlMode.PercentOutput, 0.0)
    }
}