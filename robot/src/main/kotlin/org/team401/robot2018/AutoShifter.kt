package org.team401.robot2018

import org.snakeskin.ShifterState
import org.snakeskin.ShifterState.*
import org.team401.robot2018.Constants.DrivetrainParameters.MAX_AMP_DRAW
import org.team401.robot2018.Constants.DrivetrainParameters.DELTA
import org.team401.robot2018.Constants.DrivetrainParameters.SPEED_THRESHOLD

//currentTime and lastShiftTime expected in ms
fun shiftAuto(currentTime: Long, lastShiftTime: Long, currentAmpDraw: Double, desiredVel: Double, currentVel: Double, currentGear: ShifterState): ShifterState {

    if (currentAmpDraw >= MAX_AMP_DRAW) return LOW

    else {

        if(currentTime - lastShiftTime <= 250) return currentGear
        //if we have a high gear and our velocity is low, it probably means we're stuck against a wall
        //so we don't force the robot, we return a low gear
        if(currentGear == HIGH && currentVel < SPEED_THRESHOLD) return LOW

        if (currentVel - desiredVel > DELTA) return LOW
        else if (currentVel - desiredVel < -DELTA) return HIGH
        //the velocity change is minor, so the gear ratio isn't changed
        else return currentGear
    }
}