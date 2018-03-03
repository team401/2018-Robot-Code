package org.team401.robot2018.auto

import org.team401.robot2018.auto.steps.AutoStep
import org.team401.robot2018.auto.steps.DelayStep
import org.team401.robot2018.auto.steps.SubSequence
import org.team401.robot2018.etc.StepAdder
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
    private fun scaleFirst(): Boolean {
        //If we aren't aligned with the switch, always go to the scale
        if (!robotPos.alignedWith(switch)) {
            return true
        }

        //If we are aligned with the switch and the scale, go to the scale
        if (robotPos.alignedWith(switch) && robotPos.alignedWith(scale)) {
            return true
        }

        //If we are aligned with the switch and not the scale, evaluate whether or not our teammates can do switch in auto
        return true //TODO add logic
    }

    override fun assembleAuto(add: StepAdder) {
        add(DelayStep(baseDelay)) //Wait for the base delay

        if (target != AutoTarget.NOTHING) {
            Routines.setup() //Run common setup tasks (stow intake, elevator to high gear, lock elevator in place)

            when (target) {
                AutoTarget.BASELINE_ONLY -> {
                    Routines.drive(robotPos, FieldElements.baseline(switch.invert()), SubSequence(*Commands.DeployAndWait))
                    //AUTO END
                }

                AutoTarget.SWITCH_ONLY -> {
                    Routines.drive(robotPos, FieldElements.switch(switch), SubSequence(*Commands.DeployAndWait)) //Drive and deploy
                    Routines.score() //Score cube
                    //AUTO END
                }

                AutoTarget.SCALE_ONLY -> {
                    Routines.drive(robotPos, FieldElements.scale(scale), SubSequence(*Commands.DeployAndWait, Commands.ScaleAfterUnfold))
                    Routines.score()
                    //AUTO END
                }

                AutoTarget.FULL -> {

                }

                else -> {
                    //AUTO END
                }
            }
        }
        //AUTO END
    }
}