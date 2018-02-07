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
 * Gains for a PIDFVAH controller
 *
 * @param p Proportional gain
 * @param i Integral gain
 * @param d Derivative gain
 * @param f Feed-forward gain
 * @param v Velocity scalar.  Should be 1 / system max velocity
 * @param a Acceleration scalar.  Used to increase or decrease overall acceleration.  Usually 0.0
 * @param h Heading correction gain.
 */
data class PIDFVAH(val p: Double = 0.0,
                   val i: Double = 0.0,
                   val d: Double = 0.0,
                   val f: Double = 0.0,
                   val v: Double = 0.0,
                   val a: Double = 0.0,
                   val h: Double = 0.0)