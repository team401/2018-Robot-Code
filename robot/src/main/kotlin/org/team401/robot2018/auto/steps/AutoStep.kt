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
abstract class AutoStep(var done: Boolean = false) {
    enum class State {
        ENTRY,
        ACTION,
        EXIT,
        CONTINUE
    }

    var state = State.ENTRY; private set

    fun reset() {
        state = State.ENTRY
    }

    fun doContinue() = state == State.CONTINUE

    fun tick() {
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

    abstract fun entry()
    abstract fun exit()
    abstract fun action()
}