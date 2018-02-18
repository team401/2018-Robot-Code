package org.team401.robot2018.auto

import edu.wpi.first.wpilibj.smartdashboard.SendableChooser
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import openrio.powerup.MatchData
import org.snakeskin.auto.AutoLoop
import org.team401.robot2018.Constants
import org.team401.robot2018.auto.motion.RioProfileRunner
import org.team401.robot2018.auto.steps.AutoStep
import org.team401.robot2018.auto.steps.DelayStep
import org.team401.robot2018.auto.steps.StepGroup
import org.team401.robot2018.subsystems.Drivetrain

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

    private val robotPosSelector = SendableChooser<RobotPosition>()
    private val autoTargetSelector = SendableChooser<AutoTarget>()

    fun publish() {
        robotPosSelector.addDefault("Middle", RobotPosition.DS_MID)
        robotPosSelector.addObject("Left", RobotPosition.DS_LEFT)
        robotPosSelector.addObject("Right", RobotPosition.DS_RIGHT)

        autoTargetSelector.addDefault("Scale -> Switch", AutoTarget.SCALE_SWITCH)
        autoTargetSelector.addObject("Scale", AutoTarget.SCALE)
        autoTargetSelector.addObject("Switch", AutoTarget.SWITCH)
        autoTargetSelector.addObject("Do Nothing", AutoTarget.NONE)
    }

    var robotPos = RobotPosition.DS_MID
    var target = AutoTarget.SCALE_SWITCH
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
        robotPos = robotPosSelector.selected
        target = autoTargetSelector.selected
        baseDelay = SmartDashboard.getNumber("baseDelay", 0.0).toLong()
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
                "$start-${end}_L.csv",
                "$start-${end}_R.csv"
        )

        val steps = arrayListOf<AutoStep>(step)
        steps.addAll(otherActions.toList())

        return StepGroup(steps)
    }

    private fun assembleAuto() {
        sequence.run {
            add(DelayStep(baseDelay)) //Wait an initial amount of time

            add(Commands.HoldElevator) //Hold the carriage in place
            add(Commands.ElevatorHolderClamp) //Clamp down on the box

            //Pass 1 (MP Init)
            when (target) {
                AutoTarget.SWITCH -> {
                    add(mpStep(robotPos.toString() , "SWITCH_$switch", Commands.DeployElevator)) //Drive and deploy
                }
                AutoTarget.SCALE, AutoTarget.SCALE_SWITCH -> {
                    add(mpStep(robotPos.toString(), "SCALE_$scale", Commands.DeployElevator)) //Drive and deploy
                }
                else -> {}
            }

            //Pass 2 (SWITCH, SCALE scoring sequence, SCALE_SWITCH initial scoring sequence
            add(Commands.WaitForDeploy) //Wait for the elevator to finish deploying
            add(Commands.ElevatorHolderUnclamp) //Unclamp the carriage
            add(Commands.ElevatorKickerScore) //Kick the box out
            add(DelayStep(Delays.SCORE)) //Wait for cube to leave robot
            add(Commands.ElevatorKickerRetract) //Retract the kicker

            when (target) {
                AutoTarget.SCALE_SWITCH -> {
                    add(mpStep("SCALE_$scale", "SWITCH_$switch", Commands.HomeElevator))
                    //TODO intake
                    add(Commands.ElevatorToSwitch)
                    add(Commands.ElevatorKickerScore)
                    add(DelayStep(Delays.SCORE))
                    add(Commands.ElevatorKickerRetract)
                }
                else -> {}
            }

            //Pass 3 (SCALE_SWITCH final scoring sequence)
            when (target) {
                AutoTarget.SCALE_SWITCH -> {
                    add(mpStep("SCALE_$scale", "SWITCH_$switch", Commands.HomeElevator)) //Drive and home
                    add(Commands.ElevatorToGround) //Bring the elevator to the ground
                    //TODO Intake (will include box acquisition and elevator clamping)
                    add(Commands.ElevatorToSwitch) //Elevator to the switch scoring position
                    add(Commands.ElevatorHolderUnclamp) //Unclamp the carriage
                    add(Commands.ElevatorKickerScore) //Kick the box out
                    add(DelayStep(Delays.SCORE)) //Wait for the cube to leave the robot
                    add(Commands.ElevatorKickerRetract) //Retract the kicker
                }
                else -> {}
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