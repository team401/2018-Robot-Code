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
enum class AutoTarget(val prettyName: String) {
    FULL("Full Auto"),
    NOTHING("Do Nothing"),
    BASELINE_ONLY("Baseline Only"),
    SWITCH_ONLY("Switch Only"),
    SCALE_ONLY("Scale Only");

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