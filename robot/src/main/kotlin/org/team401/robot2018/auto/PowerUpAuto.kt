package org.team401.robot2018.auto

import edu.wpi.first.wpilibj.smartdashboard.SendableChooser
import openrio.powerup.MatchData
import org.snakeskin.auto.AutoLoop
import org.team401.robot2018.auto.steps.AutoStep

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

object PowerUpAuto: AutoLoop {
    override val rate = 10L

    private val robotPosSelector = SendableChooser<RobotPosition>()
    private val autoTargetSelector = SendableChooser<AutoTarget>()

    fun publish() {
        robotPosSelector.addDefault("Middle", RobotPosition.MID)
        robotPosSelector.addObject("Left", RobotPosition.LEFT)
        robotPosSelector.addObject("Right", RobotPosition.RIGHT)

        autoTargetSelector.addDefault("Scale -> Switch", AutoTarget.SCALE_SWITCH)
        autoTargetSelector.addObject("Scale", AutoTarget.SCALE)
        autoTargetSelector.addObject("Switch", AutoTarget.SWITCH)
        autoTargetSelector.addObject("Switch -> Scale", AutoTarget.SWITCH_SCALE)
        autoTargetSelector.addObject("Do Nothing", AutoTarget.NONE)
    }

    var robotPos = RobotPosition.MID
    var target = AutoTarget.SCALE_SWITCH
    var switch = MatchData.OwnedSide.UNKNOWN
    var scale = MatchData.OwnedSide.UNKNOWN

    var sequence = arrayListOf<AutoStep>()

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
    }

    private fun assembleAuto(): List<AutoStep> {
        sequence.run {
            add(Commands.DeployElevator)


        }

        return listOf()
    }


    override fun entry() {
        fetchSD()
        fetchFieldLayout()
        sequence.clear()
        assembleAuto()
        sequence.forEach {
            it.start()
        }
    }

    override fun action() {
        sequence.forEach {
            if (!it.done) {
                it.tick()
            }
        }
    }

    override fun exit() {
        sequence.forEach {
            it.stop()
        }
    }

}