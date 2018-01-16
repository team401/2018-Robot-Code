package org.team401.robot2018.auto

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
interface AutoStep {
    fun reset()
    fun start()
    fun stop()
    fun tick()
}