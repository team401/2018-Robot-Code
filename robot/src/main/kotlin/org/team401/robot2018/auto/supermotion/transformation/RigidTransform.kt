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
class RigidTransform(var translation: Translation = Translation(), var rotation: Rotation = Rotation()) {
    constructor(other: RigidTransform) : this(Translation(other.translation), Rotation(other.rotation))


    companion object: TransformationCompanion<RigidTransform> {
        override val identity = RigidTransform()
        const val EPSILON = 1E-9

        fun fromTranslation(translation: Translation) = RigidTransform(translation, Rotation())
        fun fromRotation(rotation: Rotation) = RigidTransform(Translation(), rotation)

        fun exp(delta: Twist): RigidTransform {
            val sinTheta = Math.sin(delta.dTheta)
            val cosTheta = Math.cos(delta.dTheta)
            val s: Double
            val c: Double
            if (Math.abs(delta.dTheta) < EPSILON) {
                s = 1.0 - 1.0 / 6.0 * delta.dTheta * delta.dTheta
                c = .5 * delta.dTheta
            } else {
                s = sinTheta / delta.dTheta
                c = (1.0 - cosTheta) / delta.dTheta
            }
            return RigidTransform(
                    Translation(delta.dx * s - delta.dy * c, delta.dx * c + delta.dy * s),
                    Rotation(cosTheta, sinTheta, false)
            )
        }

        fun log(transform: RigidTransform) {
            val dTheta = transform.rotation.getRadians()
        }
    }

}