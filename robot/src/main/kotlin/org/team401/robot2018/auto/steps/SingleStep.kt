package org.team401.robot2018.auto.steps

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
abstract class SingleStep: AutoStep(true) {
    override fun action(currentTime: Double, lastTime: Double) {}
    override fun exit(currentTime: Double) {}
}