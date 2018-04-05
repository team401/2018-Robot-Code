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

class ParallelSteps(): AutoStep() {
    constructor(stepsIn: List<AutoStep>) : this() {
        steps.addAll(stepsIn)
    }

    constructor(vararg stepsIn: AutoStep) : this() {
        steps.addAll(stepsIn)
    }

    private val steps = arrayListOf<AutoStep>()

    override fun entry(currentTime: Double) {
        steps.forEach {
            it.entry(currentTime)
        }
    }

    override fun action(currentTime: Double, lastTime: Double) {
        steps.forEach {
            if (!it.done) {
                it.action(currentTime, lastTime)
            }
        }
        if (steps.all { it.done } || steps.size == 0) {
            done = true
        }
    }

    override fun exit(currentTime: Double) {
        steps.forEach {
            it.exit(currentTime)
        }
    }
}