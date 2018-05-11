package org.team401.robot2018.auto

import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.Timer
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import openrio.powerup.MatchData
import org.snakeskin.auto.AutoLoop
import org.snakeskin.factory.ExecutorFactory
import org.team401.robot2018.auto.steps.AutoStep
import java.sql.Driver
import java.util.concurrent.TimeUnit

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
    private val executor = ExecutorFactory.getExecutor("Auto")

    //DATA
    private val autoTargetSelector = AutoTarget.toSendableChooser()

    fun startTasks() {
        val ds = DriverStation.getInstance()
        executor.scheduleAtFixedRate({
            if (ds.isDisabled) {
                fetchSD()
                preAuto()
            }
        }, 0L, 100L, TimeUnit.MILLISECONDS)
    }

    fun publish() {
        SmartDashboard.putData("Auto Mode", autoTargetSelector)
        SmartDashboard.putNumber("Base Delay", 0.0)
    }

    protected var robotPos = RobotPosition.DS_CENTER; private set
    protected var target = AutoTarget.NOTHING; private set
    protected var switchSide = MatchData.OwnedSide.UNKNOWN; private set
    protected var scaleSide = MatchData.OwnedSide.UNKNOWN; private set
    protected var baseDelay = 0.0; private set

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
        target = autoTargetSelector.selected
        robotPos = target.robotPosition
        baseDelay = SmartDashboard.getNumber("Base Delay", 0.0)
    }

    //AUTO MANAGER
    private val sequence = arrayListOf<AutoStep>()
    private var sequenceIdx = 0
    private val adder: (AutoStep) -> Unit = { sequence.add(it) }

    override val rate = 5L

    abstract fun preAuto()
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

    override fun action(currentTime: Double, lastTime: Double) {
        if (sequenceIdx < sequence.size) {
            sequence[sequenceIdx].tick(currentTime, lastTime)
            if (sequence[sequenceIdx].doContinue()) {
                sequenceIdx++
            }
        } else {
            done = true
        }
    }

    override fun exit() {
        val time = Timer.getFPGATimestamp()
        sequence.forEach {
            if (it.state != AutoStep.State.CONTINUE) {
                it.exit(time)
            }
        }
    }
}