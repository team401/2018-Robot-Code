package org.team401.robot2018.auto.steps

/*
 * 2018-Robot-Code - Created on 1/25/18
 * Author: Cameron Earle
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at project root
 */

/**
 * @author Cameron Earle
 * @version 1/25/18
 */

class DelayStep(val time: Long): SingleStep() {
    override fun entry() {
        Thread.sleep(time)
    }
}