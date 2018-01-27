package org.team401.robot2018

import com.ctre.phoenix.ParamEnum
import com.ctre.phoenix.motorcontrol.IMotorControllerEnhanced

/*
 * 2018-Robot-Code - Created on 1/17/18
 * Author: Cameron Earle
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at project root
 */

/**
 * @author Cameron Earle
 * @version 1/17/18
 */

fun IMotorControllerEnhanced.configZeroPosOnReverseLimit(enable: Boolean, timeout: Int = 0) {
    configSetParameter(ParamEnum.eClearPosOnLimitR, if (enable) 1.0 else 0.0, 0, 0, timeout)
}

fun IMotorControllerEnhanced.configZeroPosOnForwardLimit(enable: Boolean, timeout: Int = 0) {
    configSetParameter(ParamEnum.eClearPosOnLimitF, if (enable) 1.0 else 0.0, 0, 0, timeout)
}

fun IMotorControllerEnhanced.pidf(p: Double = 0.0, i: Double = 0.0, d: Double = 0.0, f: Double = 0.0, slot: Int = 0, timeout: Int = 0) {
    config_kP(slot, p, timeout)
    config_kI(slot, i, timeout)
    config_kD(slot, d, timeout)
    config_kF(slot, f, timeout)
}