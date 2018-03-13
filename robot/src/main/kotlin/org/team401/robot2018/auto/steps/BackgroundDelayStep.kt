package org.team401.robot2018.auto.steps

class BackgroundDelayStep(val time: Long, val period: Long): AutoStep() {
    var counter = 0

    override fun entry() {
        done = false
        counter = 0
    }

    override fun action() {
        done = (counter++ * period >= time)
    }

    override fun exit() {}
}