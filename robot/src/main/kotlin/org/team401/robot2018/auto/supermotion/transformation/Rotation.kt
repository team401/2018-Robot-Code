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
class Rotation(var cos: Double = 1.0, var sin: Double = 0.0, normalize: Boolean = false): Interpolatable<Rotation> {
    init {
        if (normalize) normalize()
    }

    constructor(other: Rotation) : this(other.cos, other.sin)
    constructor(direction: Translation, normalize: Boolean) : this(direction.x, direction.y, normalize)

    fun normalize() {
        val magnitude = Math.hypot(cos, sin)
        if (magnitude > EPSILON) {
            sin /= magnitude
            cos /= magnitude
        } else {
            sin = 0.0
            cos = 1.0
        }
    }

    val tan: Double
    get() = if (Math.abs(cos) < EPSILON)
                if (sin >= 0.0) Double.POSITIVE_INFINITY
                else Double.NEGATIVE_INFINITY
            else sin / cos

    fun getRadians() = Math.atan2(sin, cos)
    fun getDegrees() = Math.toDegrees(getRadians())

    fun rotateBy(other: Rotation) = Rotation(
            cos * other.cos - sin * other.sin,
            cos * other.sin + sin * other.cos,
            true
    )

    fun normal() = Rotation(-sin, cos, false)
    fun inverse() = Rotation(cos, -sin, false)

    fun toTranslation() = Translation(cos, sin)

    fun isParallel(other: Rotation) = MotionMath.epsilonEquals(
            Translation.cross(toTranslation(), other.toTranslation()),
            0.0,
            EPSILON
    )

    override fun interpolate(other: Rotation, x: Double): Rotation {
        if (x <= 0) return Rotation(this)
        else if (x >= 1) return Rotation(other)
        val angleDiff = inverse().rotateBy(other).getRadians()
        return rotateBy(fromRadians(angleDiff * x))
    }

    companion object: TransformationCompanion<Rotation> {
        override val identity = Rotation()

        const val EPSILON = 1E-9

        fun fromRadians(angle: Double) = Rotation(Math.cos(angle), Math.sin(angle), false)
        fun fromDegrees(angle: Double) = fromRadians(Math.toRadians(angle))
    }

    override fun toString() = "(${MotionMath.df.format(getDegrees())} deg)"
}