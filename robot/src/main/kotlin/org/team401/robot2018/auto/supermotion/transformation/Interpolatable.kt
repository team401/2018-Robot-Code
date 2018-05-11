package org.team401.robot2018.auto.supermotion.transformation

/*
 * 2018-Robot-Code - Created on 3/31/18
 * Author: Cameron Earle
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at project root
 */

/**
 * @author Cameron Earle
 * @version 3/31/18
 */
interface Interpolatable<T> {
    fun interpolate(other: T, x: Double): T
}