package org.team401.robot2018.auto

import edu.wpi.first.wpilibj.smartdashboard.SendableChooser
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import openrio.powerup.MatchData
import org.snakeskin.auto.AutoLoop
import org.team401.robot2018.auto.motion.GyroTurn
import org.team401.robot2018.auto.motion.RioProfileRunner
import org.team401.robot2018.auto.steps.AutoStep
import org.team401.robot2018.auto.steps.DelayStep
import org.team401.robot2018.auto.steps.LambdaStep
import org.team401.robot2018.auto.steps.StepGroup
import org.team401.robot2018.auto.steps.SubSequence
import org.team401.robot2018.etc.Constants
import org.team401.robot2018.etc.invert
import org.team401.robot2018.subsystems.Drivetrain
import kotlin.system.exitProcess

/*
 * 2018-Robot-Code - Created on 1/23/18
 * Author: Cameron Earle
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at project root
 */

/**
 * @author Cameron Earle
 * @version 1/23/18
 */

object PowerUpAuto: AutoLoop() {
    object Delays {
        const val SCORE = 1000L //ms
    }

    override val rate = 10L

    private val robotPosSelector = RobotPosition.toSendableChooser()
    private val autoTargetSelector = AutoTarget.toSendableChooser()

    fun publish() {
        SmartDashboard.putData("Robot Position", robotPosSelector)
        SmartDashboard.putData("Auto Target", autoTargetSelector)
    }

    var robotPos = RobotPosition.DS_MID
    var target = AutoTarget.FULL
    var switch = MatchData.OwnedSide.UNKNOWN
    var scale = MatchData.OwnedSide.UNKNOWN
    var baseDelay = 0L

    var sequence = arrayListOf<AutoStep>()
    private var sequenceIdx = 0

    /**
     * Polls the field for data until valid data is found
     * Runs at a 1 ms rate to ensure we get data as fast as possible
     */
    private fun fetchFieldLayout() {
        while (switch == MatchData.OwnedSide.UNKNOWN || scale == MatchData.OwnedSide.UNKNOWN) {
            switch = MatchData.getOwnedSide(MatchData.GameFeature.SWITCH_NEAR)
            scale = MatchData.getOwnedSide(MatchData.GameFeature.SCALE)
            Thread.sleep(1)
        }
    }

    /**
     * Gets various info from SmartDashboard
     */
    private fun fetchSD() {
        //TODO add back proper SD reading
        robotPos = RobotPosition.DS_MID//robotPosSelector.selected
        target = AutoTarget.FULL//autoTargetSelector.selected
        baseDelay = 0L//SmartDashboard.getNumber("Base Delay", 0.0).toLong()
    }

    /**
     * Builds a motion profile step group for the left and
     * right sides of the drivetrain using the given entry and end
     */
    private fun mpStep(start: String, end: String, vararg otherActions: AutoStep): StepGroup {
        val leftMaster = Drivetrain.left.master
        val rightMaster = Drivetrain.right.master
        val imu = Drivetrain.imu

        val step = RioProfileRunner(
                leftMaster,
                rightMaster,
                imu,
                Constants.DrivetrainParameters.LEFT_PDVA,
                Constants.DrivetrainParameters.RIGHT_PDVA,
                Constants.DrivetrainParameters.HEADING_GAIN
        )

        step.loadPoints(
                "/home/lvuser/profiles/$start-${end}_L.csv",
                "/home/lvuser/profiles/$start-${end}_R.csv"
        )

        val steps = arrayListOf<AutoStep>(step)
        steps.addAll(otherActions.toList())

        return StepGroup(steps)
    }


    private const val TURN_AROUND_GAIN = 0.0024
    private const val TURN_AROUND_F = 0.2
    private const val TURN_AROUND_ERROR = 2.0

    private fun turnAroundCCW() = GyroTurn(
            Drivetrain.left.master,
            Drivetrain.right.master,
            Drivetrain.imu,
            180.0,
            TURN_AROUND_GAIN,
            TURN_AROUND_F,
            TURN_AROUND_ERROR
    )

    private fun turnAroundCW() = GyroTurn(
            Drivetrain.left.master,
            Drivetrain.right.master,
            Drivetrain.imu,
            -180.0,
            TURN_AROUND_GAIN,
            TURN_AROUND_F,
            TURN_AROUND_ERROR
    )

    private fun assembleAuto() {
        sequence.run {
            add(DelayStep(baseDelay)) //Wait for the base delay

            //Identify target
            when (target) {
                AutoTarget.NOTHING -> {} //Do nothing
                //Baseline only mode
                AutoTarget.BASELINE_ONLY -> {
                    when (robotPos) {
                        //If we start on the left or right, just drive straight
                        RobotPosition.DS_LEFT, RobotPosition.DS_RIGHT -> {
                            add(mpStep("DS_LEFT_RIGHT", "BASELINE"))
                        }

                        //If we start in the middle, drive to the opposite side from our switch
                        //to allow other teams to go to the active switch side
                        RobotPosition.DS_MID -> {
                            add(mpStep("DS_MID", "BASELINE_${switch.invert()}"))
                        }
                    }
                    //AUTO END
                }
                AutoTarget.SWITCH_ONLY -> {
                    add(mpStep(robotPos.toString(), "SWITCH_$switch"))
                    //TODO add scoring stuff
                    //AUTO END
                }
                AutoTarget.SCALE_ONLY -> {
                    add(mpStep(robotPos.toString(), "SCALE_$scale"))
                }
                AutoTarget.FULL -> {
                    when (robotPos) {
                        RobotPosition.DS_MID -> {
                            add(mpStep(robotPos.toString(), "SCALE_$scale"))
                            //TODO scale score
                            add(mpStep("SCALE_$scale", "SWITCH_$switch"))
                            //TODO switch score
                            //AUTO END
                        }
                        RobotPosition.DS_LEFT, RobotPosition.DS_RIGHT -> {
                            if (robotPos.alignedWith(scale)) {
                                add(mpStep(robotPos.toString(), "SCALE_$scale"))
                                //TODO scale score
                                add(mpStep("SCALE_$scale", "SWITCH_$switch"))
                                //TODO switch score
                            } else {
                                if (robotPos.alignedWith(switch)) {
                                    add(mpStep(robotPos.toString(), "SWITCH_$switch"))
                                    //TODO switch score
                                    add(mpStep("SWITCH_$switch", "SWITCH_$scale"))
                                    //TODO intake cube
                                    add(mpStep("SWITCH_${switch.invert()}", "SCALE_$scale"))
                                    //TODO scale score
                                } else {
                                    add(mpStep(robotPos.toString(), "SCALE_$scale"))
                                    //TODO scale score
                                    add(mpStep("SCALE_$scale", "SWITCH_$switch"))
                                    //TODO switch score
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun entry() {
        done = false
        fetchSD()
        fetchFieldLayout()
        sequence.clear()
        sequenceIdx = 0
        assembleAuto()
        sequence.forEach {
            it.reset()
        }
    }

    override fun action() {
        if (sequenceIdx < sequence.size) {
            sequence[sequenceIdx].tick()
            if (sequence[sequenceIdx].doContinue()) {
                sequenceIdx++
            }
        } else {
            done = true
        }
    }

    override fun exit() {
        sequence.forEach {
            if (it.state != AutoStep.State.CONTINUE) {
                it.exit()
            }
        }
    }

}