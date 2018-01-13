package org.team401.robot2018

import org.snakeskin.dsl.HumanControls

/*
 * 2018-Robot-Code - Created on 1/13/18
 * Author: Cameron Earle
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at project root
 */

/**
 * @author Cameron Earle
 * @version 1/13/18
 */

val LeftStick = HumanControls.t16000m(0) {
    invertAxis(Axes.PITCH)
}

val RightStick = HumanControls.t16000m(1)

val MasherBox = HumanControls.saitekButtonBox(2)