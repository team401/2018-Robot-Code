package org.team401.robot2018.auto

import org.team401.robot2018.auto.motion.TuningRioProfileRunner
import org.team401.robot2018.auto.steps.AutoStep
import org.team401.robot2018.subsystems.Drivetrain

/*
 * 2018-Robot-Code - Created on 3/5/18
 * Author: Cameron Earle
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at project root
 */

/**
 * @author Cameron Earle
 * @version 3/5/18
 */

object TestAuto: RobotAuto() {
    override fun assembleAuto(add: (AutoStep) -> Unit) {
        add(TuningRioProfileRunner(Drivetrain.left.master, Drivetrain.right.master, Drivetrain.imu, "tuning")) //Example
    }
}