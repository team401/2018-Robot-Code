package org.team401.robot2018.auto

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import openrio.powerup.MatchData
import org.snakeskin.auto.AutoLoop
import org.team401.robot2018.auto.steps.AutoStep

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

    fun publish() {
        SmartDashboard.putData("Robot Position", robotPosSelector)
        SmartDashboard.putData("Auto Target", autoTargetSelector)
        SmartDashboard.putBoolean("Partner Switch", teammatesCanDoSwitch)
        SmartDashboard.putNumber("Base Delay", 0.0)
    }

    protected var robotPos = RobotPosition.DS_CENTER; private set
    protected var target = AutoTarget.SWITCH_ONLY; private set
    protected var switchSide = MatchData.OwnedSide.UNKNOWN; private set
    protected var scaleSide = MatchData.OwnedSide.UNKNOWN; private set
    protected var teammatesCanDoSwitch = false; private set
    protected var baseDelay = 0L; private set

    /**
     * Polls the field for data until valid data is found
     * Runs at a 1 ms rate to ensure we get data as fast as possible
     */
    private fun fetchFieldLayout() {
        while (switchSide == MatchData.OwnedSide.UNKNOWN || scaleSide == MatchData.OwnedSide.UNKNOWN) {
            switchSide = MatchData.getOwnedSide(MatchData.GameFeature.SWITCH_NEAR)
            scaleSide = MatchData.getOwnedSide(MatchData.GameFeature.SCALE)
            Thread.sleep(1)
        }
    }

    /**
     * Gets various info from SmartDashboard
     */
    private fun fetchSD() {
        //TODO add back proper SD reading
        robotPos = RobotPosition.DS_LEFT//robotPosSelector.selected
        target = AutoTarget.FULL//autoTargetSelector.selected
        teammatesCanDoSwitch = false//SmartDashboard.getBoolean("Partner Switch", false)
        baseDelay = 0L//SmartDashboard.getNumber("Base Delay", 0.0).toLong()
    }

    //AUTO MANAGER
    private val sequence = arrayListOf<AutoStep>()
    private var sequenceIdx = 0
    private val adder: (AutoStep) -> Unit = { sequence.add(it) }

    override val rate = 10L

    abstract fun assembleAuto(add: (AutoStep) -> Unit)

    override fun entry() {
        Routines.add = adder //Set the routines object to use this loop's adder
        done = false
        fetchSD()
        fetchFieldLayout()
        sequence.clear()
        sequenceIdx = 0
        assembleAuto(adder)
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