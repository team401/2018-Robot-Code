package org.team401.robot2018.auto.steps

/*
 * 2018-Robot-Code - Created on 1/23/18
 * Author: Cameron Earle
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at project root
 */

/**
 * @author Cameron Earle
 * @version 1/23/18
 */

class LambdaStep(val lambda: () -> Unit): SingleStep() {
    override fun start() {
        lambda()
    }
}