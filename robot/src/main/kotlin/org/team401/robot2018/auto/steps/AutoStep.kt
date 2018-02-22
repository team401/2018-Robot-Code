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
    enum class State {
        ENTRY,
        ACTION,
        EXIT,
        CONTINUE
    }

    var state = State.ENTRY; private set

    override fun reset() {
        state = State.ENTRY
    }

    fun doContinue() = state == State.CONTINUE

    override fun tick() {
        when (state) {
            State.ENTRY -> {
                entry()
                state = State.ACTION
            }
            State.ACTION -> {
                action()
                if (done) {
                    state = State.EXIT
                }
            }
            State.EXIT -> {
                exit()
                state = State.CONTINUE
            }
            else -> {}
        }
    }

    override abstract fun entry()
    override abstract fun exit()
    override abstract fun action()
}