package org.team401.robot2018.auto.steps

/*
 * 2018-Robot-Code - Created on 2/17/18
 * Author: Cameron Earle
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at project root
 */

/**
 * @author Cameron Earle
 * @version 2/17/18
 */

/**
 * Waits for a condition to become true before continuing
 */
class WaitForStep(val condition: () -> Boolean): AutoStep() {
    override fun entry() {}
    override fun action() {
        done = condition()
    }
    override fun exit() {}
}