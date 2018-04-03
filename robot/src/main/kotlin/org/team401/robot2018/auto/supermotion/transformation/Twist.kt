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
class Twist(val dx: Double, val dy: Double, val dTheta: Double) {
    fun scaled(scale: Double) = Twist(dx * scale, dy * scale, dTheta * scale)

    companion object: TransformationCompanion<Twist> {
        override val identity = Twist(0.0, 0.0, 0.0)
    }
}