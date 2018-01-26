package org.team401.robot2018

import org.snakeskin.logic.LockingDelegate
import org.snakeskin.publish.Publisher

/*
 * 2018-Robot-Code - Created on 1/16/18
 * Author: Cameron Earle
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at project root
 */

/**
 * @author Cameron Earle
 * @version 1/16/18
 */

object Signals {
    var elevatorPosition by Publisher(0.0)
    var elevatorHomed by Publisher(false)
}