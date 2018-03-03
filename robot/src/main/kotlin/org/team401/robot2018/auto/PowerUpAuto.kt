package org.team401.robot2018.auto

import org.team401.robot2018.auto.steps.AutoStep
import org.team401.robot2018.auto.steps.DelayStep
import org.team401.robot2018.etc.invert

/*
 * 2018-Robot-Code - Created on 1/23/18
 * Author: Cameron Earle
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at project root
 */

/**
 * @author Cameron Earle
 * @version 1/23/18
 */

object PowerUpAuto: RobotAuto() {
    override fun assembleAuto(add: (AutoStep) -> Unit) {
        add(DelayStep(baseDelay)) //Wait for the base delay

        //Identify target
        when (target) {
            AutoTarget.NOTHING -> {} //Do nothing
            //Baseline only mode
            AutoTarget.BASELINE_ONLY -> {
                when (robotPos) {
                    //If we start on the left or right, just drive straight
                    RobotPosition.DS_LEFT, RobotPosition.DS_RIGHT -> {
                        add(mpStep("DS_LEFT_RIGHT", "BASELINE"))
                    }

                    //If we start in the middle, drive to the opposite side from our switch
                    //to allow other teams to go to the active switch side
                    RobotPosition.DS_MID -> {
                        add(mpStep("DS_MID", "BASELINE_${switch.invert()}"))
                    }
                }
                //AUTO END
            }
            AutoTarget.SWITCH_ONLY -> {
                add(mpStep(robotPos.toString(), "SWITCH_$switch"))
                //TODO add scoring stuff
                //AUTO END
            }
            AutoTarget.SCALE_ONLY -> {
                add(mpStep(robotPos.toString(), "SCALE_$scale"))
            }
            AutoTarget.FULL -> {
                when (robotPos) {
                    RobotPosition.DS_MID -> {
                        add(mpStep(robotPos.toString(), "SCALE_$scale"))
                        //TODO scale score
                        add(mpStep("SCALE_$scale", "SWITCH_$switch"))
                        //TODO switch score
                        //AUTO END
                    }
                    RobotPosition.DS_LEFT, RobotPosition.DS_RIGHT -> {
                        if (robotPos.alignedWith(scale)) {
                            add(mpStep(robotPos.toString(), "SCALE_$scale"))
                            //TODO scale score
                            add(mpStep("SCALE_$scale", "SWITCH_$switch"))
                            //TODO switch score
                        } else {
                            if (robotPos.alignedWith(switch)) {
                                add(mpStep(robotPos.toString(), "SWITCH_$switch"))
                                //TODO switch score
                                add(mpStep("SWITCH_$switch", "SWITCH_$scale"))
                                //TODO intake cube
                                add(mpStep("SWITCH_${switch.invert()}", "SCALE_$scale"))
                                //TODO scale score
                            } else {
                                add(mpStep(robotPos.toString(), "SCALE_$scale"))
                                //TODO scale score
                                add(mpStep("SCALE_$scale", "SWITCH_$switch"))
                                //TODO switch score
                            }
                        }
                    }
                }
            }
        }
    }
}