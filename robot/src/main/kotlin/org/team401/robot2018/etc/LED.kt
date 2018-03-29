package org.team401.robot2018.etc

import org.snakeskin.LightLink

object LED {
    val ll = LightLink()
    const val LEFT_STRIP = 0
    const val TOP_STRIP = 1
    const val RIGHT_STRIP = 2

    fun rainbowAll(speed: Int = LightLink.Speed.SLOW) {
        ll.rainbow(speed, LEFT_STRIP)
        ll.rainbow(speed, TOP_STRIP)
        ll.rainbow(speed, RIGHT_STRIP)
    }

    fun offAll() {
        ll.off(LEFT_STRIP)
        ll.off(TOP_STRIP)
        ll.off(RIGHT_STRIP)
    }

    fun intakeGrab() {
        ll.solid(LightLink.Color.BLUE, LEFT_STRIP)
        ll.solid(LightLink.Color.BLUE, TOP_STRIP)
        ll.solid(LightLink.Color.BLUE, RIGHT_STRIP)
    }

    fun intakeOut() {
        ll.solid(LightLink.Color.BLUE, LEFT_STRIP)
        ll.solid(LightLink.Color.BLUE, TOP_STRIP)
        ll.solid(LightLink.Color.BLUE, RIGHT_STRIP)
    }

    fun intakeRetract() {
        offAll()
    }

    fun signalHaveCube() {
        ll.signal(LightLink.Color.GREEN, LEFT_STRIP)
        ll.signal(LightLink.Color.GREEN, TOP_STRIP)
        ll.signal(LightLink.Color.GREEN, RIGHT_STRIP)
    }

    fun signalEjectCube() {
        //Forgot to add red in LightLink
        ll.signal(0x01, LEFT_STRIP)
        ll.signal(0x01, TOP_STRIP)
        ll.signal(0x01, RIGHT_STRIP)
    }

    fun startClimb() {
        ll.race(LightLink.Color.YELLOW, LightLink.Speed.SLOW, LEFT_STRIP)
        ll.race(LightLink.Color.YELLOW, LightLink.Speed.SLOW, RIGHT_STRIP)
        ll.blink(LightLink.Color.YELLOW, LightLink.Speed.SLOW, TOP_STRIP)
    }

    fun gotClimb() {
        ll.signal(LightLink.Color.GREEN, TOP_STRIP)
    }

    fun finishClimb() {
        ll.breathe(LightLink.Color.GREEN, LightLink.Speed.FAST, LEFT_STRIP)
        ll.breathe(LightLink.Color.GREEN, LightLink.Speed.FAST, RIGHT_STRIP)
        ll.blink(LightLink.Color.GREEN, LightLink.Speed.SLOW, TOP_STRIP)
    }
}