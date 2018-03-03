package org.team401.robot2018.auto

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import openrio.powerup.MatchData
import org.snakeskin.auto.AutoLoop
import org.team401.robot2018.auto.motion.RioProfileRunner
import org.team401.robot2018.auto.steps.AutoStep
import org.team401.robot2018.auto.steps.StepGroup
import org.team401.robot2018.etc.Constants
import org.team401.robot2018.subsystems.Drivetrain

/*
 * 2018-Robot-Code - Created on 3/3/18
 * Author: Cameron Earle
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at project root
 */

/**
 * @author Cameron Earle
 * @version 3/3/18
 */
abstract class RobotAuto: AutoLoop() {
    //DATA
    private val robotPosSelector = RobotPosition.toSendableChooser()
    private val autoTargetSelector = AutoTarget.toSendableChooser()

    private fun publish() {
        SmartDashboard.putData("Robot Position", robotPosSelector)
        SmartDashboard.putData("Auto Target", autoTargetSelector)
    }

    protected var robotPos = RobotPosition.DS_MID; private set
    protected var target = AutoTarget.FULL; private set
    protected var switch = MatchData.OwnedSide.UNKNOWN; private set
    protected var scale = MatchData.OwnedSide.UNKNOWN; private set
    protected var baseDelay = 0L; private set

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

    //AUTO MANAGER
    private val sequence = arrayListOf<AutoStep>()
    private var sequenceIdx = 0

    override val rate = 10L

    abstract fun assembleAuto(add: (AutoStep) -> Unit)

    override fun entry() {
        done = false
        fetchSD()
        fetchFieldLayout()
        sequence.clear()
        sequenceIdx = 0
        assembleAuto { sequence.add(it) }
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

    //INBUILT STEPS
    protected fun mpStep(start: String, end: String, vararg otherActions: AutoStep): StepGroup {
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
}