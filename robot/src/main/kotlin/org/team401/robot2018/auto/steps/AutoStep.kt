package org.team401.robot2018.auto.steps

/*
 * 2018-Robot-Code - Created on 1/15/18
 * Author: Cameron Earle
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at project root
 */

/**
 * @author Cameron Earle
 * @version 1/15/18
 */
abstract class AutoStep(var done: Boolean = false) {
    abstract fun start()
    abstract fun stop()
    abstract fun tick()
}