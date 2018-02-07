package org.team401.robot2018.auto.motion

import com.ctre.phoenix.motorcontrol.IMotorControllerEnhanced
import org.team401.robot2018.auto.steps.AutoStep

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

abstract class TankMotionStep: AutoStep() {
    abstract val leftController: IMotorControllerEnhanced
    abstract val rightController: IMotorControllerEnhanced
}