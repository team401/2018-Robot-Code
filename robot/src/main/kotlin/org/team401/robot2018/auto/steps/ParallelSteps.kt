package org.team401.robot2018.auto.steps

/*
 * 2018-Robot-Code - Created on 1/25/18
 * Author: Cameron Earle
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at project root
 */

/**
 * @author Cameron Earle
 * @version 1/25/18
 */

class ParallelSteps(vararg val steps: AutoStep): AutoStep() {
    override fun entry(currentTime: Double) {
        done = false
    }

    override fun action(currentTime: Double, lastTime: Double) {
        steps.forEach {
            it.tick(currentTime, lastTime)
        }
        if (steps.all { it.doContinue() } || steps.isEmpty()) {
            done = true
        }
    }

    override fun exit(currentTime: Double) {
        steps.forEach {
            if (it.state != AutoStep.State.CONTINUE) {
                it.exit(currentTime)
            }
        }
    }
}