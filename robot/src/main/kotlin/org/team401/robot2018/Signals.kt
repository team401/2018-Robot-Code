package org.team401.robot2018

import org.snakeskin.logic.LockingDelegate

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
    var elevatorPosition by LockingDelegate(0.0)
}