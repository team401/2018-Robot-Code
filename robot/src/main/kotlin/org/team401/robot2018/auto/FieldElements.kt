package org.team401.robot2018.auto

import openrio.powerup.MatchData

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
object FieldElements {
    fun switch(side: MatchData.OwnedSide) = "SWITCH_$side"
    fun scale(side: MatchData.OwnedSide) = "SCALE_$side"
    fun baseline(side: MatchData.OwnedSide) = "BASELINE_$side"
}