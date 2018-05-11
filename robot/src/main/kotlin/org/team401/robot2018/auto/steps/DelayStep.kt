package org.team401.robot2018.auto.steps

class DelayStep(val time: Double): AutoStep() {
    var startTime = 0.0

    override fun entry(currentTime: Double) {
        done = false
        startTime = currentTime
    }

    override fun action(currentTime: Double, lastTime: Double) {
        if (currentTime - startTime >= time) {
            done = true
        }
    }

    override fun exit(currentTime: Double) {}
}