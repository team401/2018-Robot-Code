package org.team401.robot2018.auto.motionprofile

import com.ctre.phoenix.motion.MotionProfileStatus
import com.ctre.phoenix.motion.SetValueMotionProfile
import com.ctre.phoenix.motorcontrol.*
import com.ctre.phoenix.motorcontrol.can.TalonSRX
import org.snakeskin.component.TankDrivetrain
import org.snakeskin.factory.ExecutorFactory
import org.team401.robot2018.auto.steps.AutoStep
import org.team401.robot2018.etc.TalonEnums
import org.team401.robot2018.subsystems.Drivetrain
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

open class ArcProfileFollower(val drivetrain: TankDrivetrain) : AutoStep() {
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
        val timeout = 100
        val localFeedbackDevice = FeedbackDevice.CTRE_MagEncoder_Relative

        drivetrain.left.setSensor(localFeedbackDevice, timeout = timeout)
        drivetrain.right.setSensor(localFeedbackDevice, timeout = timeout)
        drivetrain.left.setPosition(0, 0, timeout)
        drivetrain.right.setPosition(0, 0, timeout)

        drivetrain.right.master.configRemoteFeedbackFilter(drivetrain.left.master.deviceID, RemoteSensorSource.TalonSRX_SelectedSensor, TalonEnums.REMOTE_O, timeout)
        drivetrain.right.master.configRemoteFeedbackFilter(drivetrain.imu.deviceID, RemoteSensorSource.GadgeteerPigeon_Yaw, TalonEnums.REMOTE_1, timeout)
        drivetrain.right.master.configSensorTerm(SensorTerm.Sum0, localFeedbackDevice, timeout)
        drivetrain.right.master.configSensorTerm(SensorTerm.Sum1, FeedbackDevice.RemoteSensor0, timeout)
        drivetrain.right.master.configSelectedFeedbackSensor(FeedbackDevice.SensorSum, TalonEnums.DISTANCE_PID, timeout)
        drivetrain.right.master.configSelectedFeedbackSensor(FeedbackDevice.RemoteSensor1, TalonEnums.HEADING_PID, timeout)
        drivetrain.right.master.configSelectedFeedbackCoefficient(.5, TalonEnums.DISTANCE_PID, timeout)
        drivetrain.right.master.configSelectedFeedbackCoefficient(3600.0/8192.0, TalonEnums.HEADING_PID, timeout)

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
        println("RUNNING PROFILE")
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
                if (status.btmBufferCnt > 5) {
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
        drivetrain.left.master.set(ControlMode.PercentOutput, 0.0)
        drivetrain.right.master.set(ControlMode.PercentOutput, 0.0)
    }
}