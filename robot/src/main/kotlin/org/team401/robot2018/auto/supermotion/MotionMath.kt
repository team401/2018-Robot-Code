package org.team401.robot2018.auto.supermotion

import java.text.DecimalFormat

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
object MotionMath {
    val df = DecimalFormat("#0.000")

    fun epsilonEquals(a: Double, b: Double, epsilon: Double): Boolean = a - epsilon <= b && a + epsilon >= b
}