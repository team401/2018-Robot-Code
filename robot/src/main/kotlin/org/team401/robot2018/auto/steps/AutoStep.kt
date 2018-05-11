package org.team401.robot2018.auto.steps

/*
 * 2018-Robot-Code - Created on 1/15/18
 * Author: Cameron Earle
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at project root
 */

/**
 * @author Cameron Earle
 * @version 1/15/18
 */
abstract class AutoStep(override var done: Boolean = false): IAutoStep {
    val isSingleStep = done
    enum class State {
        ENTRY,
        ACTION,
        EXIT,
        CONTINUE
    }

    var state = State.ENTRY; private set

    override fun reset() {
        state = State.ENTRY
        if (!isSingleStep) {
            done = false
        }
    }

    fun doContinue() = (state == State.CONTINUE)

    override fun tick(currentTime: Double, lastTime: Double) {
        when (state) {
            State.ENTRY -> {
                entry(currentTime)
                state = State.ACTION
            }
            State.ACTION -> {
                action(currentTime, lastTime)
                if (done) {
                    state = State.EXIT
                }
            }
            State.EXIT -> {
                exit(currentTime)
                state = State.CONTINUE
            }
            else -> {}
        }
    }

    override abstract fun entry(currentTime: Double)
    override abstract fun exit(currentTime: Double)
    override abstract fun action(currentTime: Double, lastTime: Double)
}