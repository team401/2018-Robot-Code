package org.team401.robot2018.auto.motion

class DriveController(val gains: DriveGains, val magnitude: Double = 1.0) {
    fun calculate(position: Double,
                  velocity: Double,
                  desiredPosition: Double,
                  desiredVelocity: Double,
                  desiredAccel: Double) =
                                    Math.max(-magnitude, Math.min(magnitude,
                                          (gains.P * (desiredPosition - position)) +
                                          (gains.V * (desiredVelocity - velocity)) +
                                          (gains.ffV * desiredVelocity) +
                                          (gains.ffA * desiredAccel)
                                    ))
}