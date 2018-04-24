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

enum class RobotPosition() {
    DS_CENTER,
    DS_LEFT,
    DS_RIGHT;

    infix fun alignedWith(side: MatchData.OwnedSide) =
            when (this) {
                DS_LEFT -> side == MatchData.OwnedSide.LEFT
                DS_CENTER -> false
                DS_RIGHT -> side == MatchData.OwnedSide.RIGHT
            }
}