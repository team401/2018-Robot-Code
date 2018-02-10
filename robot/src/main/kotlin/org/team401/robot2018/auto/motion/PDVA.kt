package org.team401.robot2018.auto.motion

/*
 * 2018-Robot-Code - Created on 2/6/18
 * Author: Cameron Earle
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at project root
 */

/**
 * @author Cameron Earle
 * @version 2/6/18
 */

/**
 * Gains for a PDVA controller
 *
 * @param p Proportional gain
 * @param d Derivative gain
 * @param v Velocity scalar.  Should be 1 / system max velocity
 * @param a Acceleration scalar.  Used to increase or decrease overall acceleration.  Usually 0.0
 */
data class PDVA(val p: Double = 0.0,
                val d: Double = 0.0,
                val v: Double = 0.0,
                val a: Double = 0.0)