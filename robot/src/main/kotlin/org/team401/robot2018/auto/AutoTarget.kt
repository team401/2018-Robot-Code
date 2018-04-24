package org.team401.robot2018.auto

import edu.wpi.first.wpilibj.smartdashboard.SendableChooser
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard

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
enum class AutoTarget(val prettyName: String, val robotPosition: RobotPosition, vararg val firstProfiles: String) {
    NOTHING("Do nothing", RobotPosition.DS_CENTER),
    CENTER_SWITCH("Center 2 Switch", RobotPosition.DS_CENTER, "DS_CENTER-SWITCH_LEFT", "DS_CENTER-SWITCH_RIGHT"),
    SWITCH_LEFT("Baseline/Switch left", RobotPosition.DS_LEFT, "DS_LEFT-SWITCH_LEFT", "BASELINE"),
    SWITCH_RIGHT("Baseline/Switch right", RobotPosition.DS_RIGHT, "DS_RIGHT-SWITCH_RIGHT", "BASELINE"),
    FULL_SCALE_LEFT("Full Scale left", RobotPosition.DS_LEFT, "DS_LEFT-SCALE_LEFT", "DS_LEFT-SCALE_RIGHT"),
    FULL_SCALE_RIGHT("Full Scale right", RobotPosition.DS_RIGHT, "DS_RIGHT-SCALE_RIGHT", "DS_RIGHT-SCALE_LEFT"),
    STAY_NEAR_LEFT("Stay near left", RobotPosition.DS_LEFT, "DS_LEFT-SCALE_LEFT", "DS_LEFT-SWITCH_LEFT", "BASELINE"),
    STAY_NEAR_RIGHT("Stay near right", RobotPosition.DS_RIGHT, "DS_RIGHT-SCALE_RIGHT", "DS_RIGHT-SWITCH_RIGHT", "BASELINE");

    companion object {
        fun toSendableChooser(): SendableChooser<AutoTarget> {
            val chooser = SendableChooser<AutoTarget>()
            values().forEachIndexed {
                index, target ->
                if (index == 0) {
                    chooser.addDefault(target.prettyName, target)
                } else {
                    chooser.addObject(target.prettyName, target)
                }
            }
            return chooser
        }
    }
}