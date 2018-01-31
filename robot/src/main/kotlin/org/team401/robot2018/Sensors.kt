package org.team401.robot2018

import edu.wpi.first.wpilibj.DriverStation
import org.snakeskin.dsl.Sensors
import org.snakeskin.sensors.BooleanSensor

/*
 * 2018-Robot-Code - Created on 1/30/18
 * Author: Cameron Earle
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at project root
 */

/**
 * @author Cameron Earle
 * @version 1/30/18
 */

val VisionStopSensor = Sensors.booleanSensor({DriverStation.getInstance().isOperatorControl && DriverStation.getInstance().matchTime <= 5}) {
    pollAt(1000)

    whenTriggered {
        Thread.sleep(10000)
        Vision.stop()
    }
}