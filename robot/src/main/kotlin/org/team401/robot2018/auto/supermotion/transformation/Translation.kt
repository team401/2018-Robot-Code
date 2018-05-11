package org.team401.robot2018.auto.supermotion.transformation

import org.team401.robot2018.auto.supermotion.MotionMath

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
class Translation(var x: Double = 0.0, var y: Double = 0.0): Interpolatable<Translation> {
    constructor(other: Translation) : this(other.x, other.y)
    constructor(start: Translation, end: Translation) : this(end.x - start.x,
                                                             end.y - start.y)

    fun norm() = Math.hypot(x, y)
    fun norm2() = x * x + y * y

    fun translateBy(other: Translation) = Translation(x + other.x, y + other.y)
    fun rotateBy(rotation: Rotation) = Translation(
            x * rotation.cos - y * rotation.sin,
            x * rotation.sin + y * rotation.cos
    )
    fun direction() = Rotation(x, y, true)
    fun inverse() = Translation(-x, -y)

    override fun interpolate(other: Translation, x: Double) = when {
            x <= 0 -> Translation(this)
            x >= 1 -> Translation(other)
            else -> extrapolate(other, x)
    }
    fun extrapolate(other: Translation, x: Double) = Translation(
            x * (other.x - this.x) + this.x,
            x * (other.y - this.y) + this.y
    )
    fun scale(scale: Double) = Translation(x * scale, y * scale)

    companion object: TransformationCompanion<Translation> {
        override val identity = Translation()

        fun dot(a: Translation, b: Translation) = a.x * b.x + a.y * b.y
        fun cross(a: Translation, b: Translation) = a.x * b.y - a.y * b.x

        fun getAngle(a: Translation, b: Translation): Rotation {
            val cos = dot(a, b) / (a.norm() * b.norm())
            if (cos.isNaN()) return Rotation()
            return Rotation.fromRadians(Math.acos(Math.min(1.0, Math.max(cos, -1.0))))
        }
    }

    override fun toString() = "(${MotionMath.df.format(x)},${MotionMath.df.format(y)})"
}
