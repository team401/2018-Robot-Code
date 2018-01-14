package org.team401.robot2018

import com.ctre.phoenix.motion.SetValueMotionProfile
import com.ctre.phoenix.motion.TrajectoryPoint
import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.IMotorControllerEnhanced
import com.ctre.phoenix.motorcontrol.can.TalonSRX
import org.snakeskin.auto.TempAutoManager
import org.snakeskin.dsl.*
import org.snakeskin.registry.*
import org.team401.robot2018.subsystems.Drivetrain
import org.team401.robot2018.subsystems.DrivetrainSubsystem
import java.io.File

/*
 * 2018-Robot-Code - Created on 1/5/18
 * Author: Cameron Earle
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at project root
 */

/**
 * @author Cameron Earle
 * @version 1/5/18
 */

@Setup fun setup() {
    Subsystems.add(DrivetrainSubsystem)
    Controllers.add(LeftStick, RightStick)

    TempAutoManager.auto = autoLoop {
        val leftFile = File("/media/sda2/PATH_L.csv")
        val rightFile = File("/media/sda2/PATH_R.csv")
        
        val leftLines = leftFile.readLines()
        val rightLines = rightFile.readLines()
        
        val leftPoints = arrayListOf<TrajectoryPoint>()
        val rightPoints = arrayListOf<TrajectoryPoint>()
        
        lateinit var leftMaster: IMotorControllerEnhanced
        lateinit var rightMaster: IMotorControllerEnhanced

        var leftCounter = 0
        var rightCounter = 0
        
        var leftDone = false
        var rightDone = false
        
        entry {
            println("-------------AUTO ENTRY-------------")

            leftCounter = 0
            rightCounter = 0

            leftDone = false
            rightDone = false

            leftPoints.clear()
            rightPoints.clear()

            Drivetrain.zero()
            leftMaster = Drivetrain.left.master
            rightMaster = Drivetrain.right.master

            leftMaster.config_kF(0, 0.5, 0)
            rightMaster.config_kF(0, 0.5, 0)


            leftMaster.set(ControlMode.MotionProfile, SetValueMotionProfile.Disable.value.toDouble())
            rightMaster.set(ControlMode.MotionProfile, SetValueMotionProfile.Disable.value.toDouble())

            leftMaster.changeMotionControlFramePeriod(20)

            leftMaster.clearMotionProfileTrajectories()
            leftMaster.clearMotionProfileHasUnderrun(0)

            rightMaster.clearMotionProfileTrajectories()
            rightMaster.clearMotionProfileHasUnderrun(0)

            leftLines.forEachIndexed {
                index, item ->
                val split = item.split(",")
                val position = split[0].toDouble()
                val velocity = split[1].toDouble()
                val point = TrajectoryPoint()
                point.position = position
                point.velocity = velocity
                point.timeDur = TrajectoryPoint.TrajectoryDuration.Trajectory_Duration_0ms
                point.zeroPos = (index == 0)
                point.isLastPoint = (index == leftPoints.lastIndex)
                leftPoints.add(point)
            }

            rightLines.forEachIndexed {
                index, item ->
                val split = item.split(",")
                val position = split[0].toDouble()
                val velocity = split[1].toDouble()
                val point = TrajectoryPoint()
                point.position = position
                point.velocity = velocity
                point.timeDur = TrajectoryPoint.TrajectoryDuration.Trajectory_Duration_0ms
                point.zeroPos = (index == 0)
                point.isLastPoint = (index == rightPoints.lastIndex)
                rightPoints.add(point)
            }

        }

        action(10) {

            if (!leftDone) {
                if (!leftMaster.isMotionProfileTopLevelBufferFull) {
                    leftMaster.pushMotionProfileTrajectory(leftPoints[leftCounter])
                    if (leftCounter < leftPoints.lastIndex) {
                        leftCounter++
                    } else {
                        leftDone = true
                    }
                }
            }

            if (!rightDone) {
                if (!rightMaster.isMotionProfileTopLevelBufferFull) {
                    rightMaster.pushMotionProfileTrajectory(rightPoints[rightCounter])
                    if (rightCounter < rightPoints.lastIndex) {
                        rightCounter++
                    } else {
                        rightDone = true
                    }
                }
            }

            leftMaster.processMotionProfileBuffer()
            rightMaster.processMotionProfileBuffer()

            leftMaster.set(ControlMode.MotionProfile, SetValueMotionProfile.Enable.value.toDouble())
            rightMaster.set(ControlMode.MotionProfile, SetValueMotionProfile.Enable.value.toDouble())

            println("LEFT: $leftCounter  RIGHT: $rightCounter")

        }

        exit {
            //println("LEFT: ${leftMaster.getSelectedSensorPosition(0)}  RIGHT: ${rightMaster.getSelectedSensorPosition(0)}")
        }
    }
}