package org.team401.robot2018.auto.steps

/*
 * 2018-Robot-Code - Created on 2/20/18
 * Author: Cameron Earle
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at project root
 */

/**
 * @author Cameron Earle
 * @version 2/20/18
 */

class SubSequence(vararg val steps: AutoStep): AutoStep() {
    private var idx = 0

    override fun entry() {
        done = false
        idx = 0
    }

    override fun action() {
        if (idx < steps.size) {
            steps[idx].tick()
            if (steps[idx].doContinue()) {
                idx++
            }
        } else {
            done = true
        }
    }

    override fun exit() {
        steps.forEach {
            if (it.state != AutoStep.State.CONTINUE) {
                it.exit()
            }
        }
    }
}