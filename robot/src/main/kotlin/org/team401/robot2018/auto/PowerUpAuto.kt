package org.team401.robot2018.auto

import edu.wpi.first.wpilibj.smartdashboard.SendableChooser
import openrio.powerup.MatchData
import org.snakeskin.auto.AutoLoop
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
        const val ELEVATOR_DEPLOY = 500L //ms
        const val PRE_SCORE = 500L //ms
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
    }

    /**
     * Builds a motion profile step group for the left and
     * right sides of the drivetrain using the given entry and end
     */
    private fun mpStep(start: String, end: String, vararg otherActions: AutoStep): StepGroup {
        val leftMaster = Drivetrain.left.master
        val rightMaster = Drivetrain.right.master

        /*
        val leftStep = MotionProfileRunner(leftMaster)
        val rightStep = MotionProfileRunner(rightMaster)

        leftStep.load("$start-${end}_L.csv")
        rightStep.load("$start-${end}_R.csv")

        val steps = arrayListOf<AutoStep>(leftStep, rightStep)
        steps.addAll(otherActions.toList())
        */

        return StepGroup()//steps)
    }

    private fun assembleAuto() {
        sequence.run {
            add(Commands.DeployElevator)
            add(DelayStep(Delays.ELEVATOR_DEPLOY)) //Wait for the elevator to deploy

            when (target) {
                AutoTarget.NONE -> {}
                AutoTarget.SWITCH -> {
                    add(mpStep(robotPos.toString(), "SWITCH_$switch", Commands.HomeElevator))
                    add(Commands.ElevatorToSwitch)
                    add(DelayStep(Delays.PRE_SCORE))
                    add(Commands.ElevatorKickerScore)
                    add(DelayStep(Delays.SCORE)) //Wait for cube to leave robot
                    add(Commands.ElevatorKickerRetract)
                }
                AutoTarget.SCALE -> {
                    add(Commands.HoldElevator)
                    add(mpStep(robotPos.toString(), "SCALE_$scale"))
                    add(Commands.ElevatorKickerScore)
                    add(DelayStep(Delays.SCORE)) //Wait for cube to leave robot
                    add(Commands.ElevatorKickerRetract)
                }
                AutoTarget.SCALE_SWITCH -> {
                    add(Commands.HoldElevator)
                    add(mpStep(robotPos.toString(), "SCALE_$scale"))
                    add(Commands.ElevatorKickerScore)
                    add(DelayStep(Delays.SCORE))
                    add(Commands.ElevatorKickerRetract)
                    add(mpStep("SCALE_$scale", "SWITCH_$switch", Commands.HomeElevator))
                    //TODO intake
                    add(Commands.ElevatorToSwitch)
                    add(Commands.ElevatorKickerScore)
                    add(DelayStep(Delays.SCORE))
                    add(Commands.ElevatorKickerRetract)
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