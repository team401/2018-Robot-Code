package org.team401.robot2018.auto.steps

/*
 * 2018-Robot-Code - Created on 2/20/18
 * Author: Cameron Earle
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at project root
 */

/**
 * @author Cameron Earle
 * @version 2/20/18
 */

interface IAutoStep {
    fun entry(currentTime: Double)
    fun action(currentTime: Double, lastTime: Double)
    fun exit(currentTime: Double)

    fun tick(currentTime: Double, lastTime: Double)
    fun reset()

    var done: Boolean
}