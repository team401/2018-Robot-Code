package org.team401.robot2018.auto.steps

class BackgroundDelayStep(val time: Long): AutoStep() {
    var startTime = 0L

    override fun entry() {
        done = false
        startTime = System.currentTimeMillis()
    }

    override fun action() {
        if (System.currentTimeMillis() - startTime >= time) {
            done = true
        }
    }

    override fun exit() {}
}