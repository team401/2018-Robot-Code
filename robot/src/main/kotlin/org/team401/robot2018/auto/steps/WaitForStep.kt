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
class WaitForStep(val timeout: Double = -1.0, val condition: () -> Boolean): AutoStep() {
    var startTime = 0.0

    override fun entry(currentTime: Double) {
        startTime = currentTime
    }

    override fun action(currentTime: Double, lastTime: Double) {
        done = ((timeout > 0.0 && currentTime - startTime >= timeout) || condition())

    }
    override fun exit(currentTime: Double) {}
}