package org.team401.robot2018.auto

import edu.wpi.first.wpilibj.DriverStation
import org.team401.robot2018.auto.steps.AutoStep
import org.team401.robot2018.auto.steps.DelayStep
import org.team401.robot2018.auto.steps.SubSequence
import org.team401.robot2018.etc.StepAdder
import org.team401.robot2018.etc.not

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
        if (robotPos.alignedWith(switch) && !robotPos.alignedWith(scale)) {
            return teammatesCanDoSwitch
        }

        //Some unhandled condition
        DriverStation.reportWarning("Auto 'scaleFirst' found an unhandled condition!  Field: ${DriverStation.getInstance().gameSpecificMessage}  Robot: $robotPos  Partner Switch: $teammatesCanDoSwitch", false)
        return true
    }

    override fun assembleAuto(add: StepAdder) {
        add(DelayStep(baseDelay)) //Wait for the base delay

        if (target != AutoTarget.NOTHING) {
            Routines.setup() //Run common setup tasks (stow intake, elevator to high gear, lock elevator in place)

            when (target) {
                AutoTarget.BASELINE_ONLY -> {
                    Routines.drive(robotPos, FieldElements.baseline(!switch), SubSequence(*Commands.HighLockDeployAndWait))
                    //AUTO END
                }

                AutoTarget.SWITCH_ONLY -> {
                    Routines.drive(robotPos, FieldElements.switch(switch), SubSequence(*Commands.HighLockDeployAndWait)) //Drive and deploy
                    Routines.score() //Score cube
                    //AUTO END
                }

                AutoTarget.SCALE_ONLY -> {
                    Routines.drive(robotPos, FieldElements.scale(scale), SubSequence(*Commands.HighLockDeployAndWait, Commands.ScaleAfterUnfold))
                    Routines.score()
                    //AUTO END
                }

                AutoTarget.FULL -> {
                    if (scaleFirst()) {
                        Routines.drive(robotPos, FieldElements.scale(scale), SubSequence(*Commands.HighLockDeployAndWait, Commands.ScaleAfterUnfold))
                        Routines.score()
                        //TODO back up and turn
                        add(Commands.IntakeToGrab)
                        Routines.drive(FieldElements.scale(scale), FieldElements.switch(switch), Commands.HomeElevator)
                        Routines.intake()
                        add(Commands.ElevatorToSwitch)
                        add(Commands.WaitForAtSwitch)
                        //TODO drive forward
                        Routines.score()
                    } else {

                    }
                }

                else -> {
                    //AUTO END
                }
            }
        }
        //AUTO END
    }
}