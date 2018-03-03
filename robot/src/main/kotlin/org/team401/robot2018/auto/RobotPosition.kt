package org.team401.robot2018.auto

import edu.wpi.first.wpilibj.smartdashboard.SendableChooser
import openrio.powerup.MatchData

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

enum class RobotPosition(val prettyName: String) {
    DS_LEFT("Left"),
    DS_MID("Middle"),
    DS_RIGHT("Right");

    fun alignedWith(side: MatchData.OwnedSide) =
            when (this) {
                DS_LEFT -> side == MatchData.OwnedSide.LEFT
                DS_MID -> false
                DS_RIGHT -> side == MatchData.OwnedSide.RIGHT
            }

    companion object {
        fun toSendableChooser(): SendableChooser<RobotPosition> {
            val chooser = SendableChooser<RobotPosition>()
            values().forEachIndexed {
                index, robotPosition ->
                if (index == 0) {
                    chooser.addDefault(robotPosition.prettyName, robotPosition)
                } else {
                    chooser.addObject(robotPosition.prettyName, robotPosition)
                }
            }
            return chooser
        }
    }
}