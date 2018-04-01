package org.team401.robot2018.auto.motion

data class DriveGains(val P: Double = 0.0,
                      val V: Double = 0.0,
                      val ffV: Double = 0.0,
                      val ffA: Double = 0.0,
                      val H: Double = 0.0)