package org.team401.robot2018.auto

import edu.wpi.first.wpilibj.DriverStation
import openrio.powerup.MatchData
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
        //If we are in the middle
        if (robotPos == RobotPosition.DS_CENTER) {
            return false
        }

        //If we aren't aligned with the switchSide, always go to the scaleSide
        if (!robotPos.alignedWith(switchSide)) {
            return true
        }

        //If we are aligned with the switchSide and the scaleSide, go to the scaleSide
        if (robotPos.alignedWith(switchSide) && robotPos.alignedWith(scaleSide)) {
            return true
        }

        //If we are aligned with the switchSide and not the scaleSide, evaluate whether or not our teammates can do switchSide in auto
        if (robotPos.alignedWith(switchSide) && !robotPos.alignedWith(scaleSide)) {
            return teammatesCanDoSwitch
        }

        //Some unhandled condition
        DriverStation.reportWarning("Auto 'scaleFirst' found an unhandled condition!  Field: ${DriverStation.getInstance().gameSpecificMessage}  Robot: $robotPos  Partner Switch: $teammatesCanDoSwitch", false)
        return false
    }

    override fun assembleAuto(add: StepAdder) {
        add(DelayStep(baseDelay)) //Wait for the base delay
        if (target != AutoTarget.NOTHING) {
            Routines.setup() //Run common setup tasks (stow intake, elevator to high gear, lock elevator in place)

            when (target) {
                AutoTarget.BASELINE_ONLY -> {
                    Routines.drive(robotPos, FieldElements.baseline(!switchSide), SubSequence(*Commands.HighLockDeployAndWait))
                    //AUTO END
                }

                AutoTarget.SWITCH_ONLY -> {
                    Routines.drive(robotPos, FieldElements.switch(switchSide), SubSequence(*Commands.HighLockDeployAndWait)) //Drive and deploy
                    Routines.score() //Score cube
                    //AUTO END
                }

                AutoTarget.SCALE_ONLY -> {
                    Routines.drive(robotPos, FieldElements.scale(scaleSide), SubSequence(*Commands.HighLockDeployAndWait, Commands.ScaleAfterUnfold))
                    Routines.score()
                    //AUTO END
                }

                AutoTarget.FULL -> {
                    if (scaleFirst()) {
                        Routines.drive(robotPos, FieldElements.scale(scaleSide), SubSequence(*Commands.HighLockDeployAndWait, Commands.ScaleAfterUnfold))
                        Routines.score()
                        Routines.drive(FieldElements.scale(scaleSide), FieldElements.backFromScale(scaleSide)) //drive back from scaleSide
                        add(Commands.IntakeToGrab) //Prepare intake for grabbing a cube
                        Routines.drive(FieldElements.backFromScale(scaleSide), FieldElements.switch(switchSide), Commands.HomeElevator) //drive and home
                        Routines.intake() //Intake the cube
                        add(Commands.ElevatorToSwitch) //Go to switch
                        add(Commands.WaitForAtSwitch) //Wait
                        Routines.drive("SWITCH_FINAL") //Drive the final way to the switch
                        Routines.score()
                    } else {
                        Routines.drive(robotPos, FieldElements.switch(switchSide), SubSequence(*Commands.HighLockDeployAndWait))
                        Routines.score()
                        Routines.drive(FieldElements.switch(switchSide), FieldElements.backFromSwitch(switchSide))
                        add(Commands.IntakeToGrab)
                        Routines.drive(FieldElements.backFromSwitch(switchSide), FieldElements.switch(scaleSide), Commands.HomeElevator) //Drive to the side of the switch aligned with the scale
                        Routines.intake()
                        Routines.drive(FieldElements.switch(scaleSide), FieldElements.backFromSwitchFront(scaleSide)) //Drive back
                        Routines.drive(FieldElements.backFromSwitchFront(scaleSide), FieldElements.scale(scaleSide), Commands.ElevatorToScale)
                        Routines.score()
                    }
                    //AUTO END
                }

                else -> {
                    //AUTO END
                }
            }
        }
        //AUTO END
    }
}