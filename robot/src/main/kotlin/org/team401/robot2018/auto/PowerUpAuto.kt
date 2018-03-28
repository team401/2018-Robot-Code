package org.team401.robot2018.auto

import com.ctre.phoenix.sensors.PigeonIMU
import edu.wpi.first.wpilibj.DriverStation
import openrio.powerup.MatchData
import org.team401.robot2018.auto.motion.ProfileLoader
import org.team401.robot2018.auto.steps.DelayStep
import org.team401.robot2018.auto.steps.SubSequence
import org.team401.robot2018.etc.StepAdder
import org.team401.robot2018.etc.encoderMissing
import org.team401.robot2018.etc.not
import org.team401.robot2018.subsystems.Drivetrain

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
    override fun preAuto() {
        fun profiles(target: Any) = arrayOf(
                "/home/lvuser/profiles/$robotPos-${target}_L.csv",
                "/home/lvuser/profiles/$robotPos-${target}_R.csv"
        )
        fun profilesSided(target: Any) = arrayOf(
                "/home/lvuser/profiles/$robotPos-${target}_LEFT_L.csv",
                "/home/lvuser/profiles/$robotPos-${target}_LEFT_R.csv",
                "/home/lvuser/profiles/$robotPos-${target}_RIGHT_L.csv",
                "/home/lvuser/profiles/$robotPos-${target}_RIGHT_R.csv"
        )
        //Here, we identify all possible first profiles and cache them
        //This is done using the selected mode and starting position
        when (target) {
            AutoTarget.NOTHING -> {}
            AutoTarget.BASELINE_ONLY -> ProfileLoader.preloadThese(*profiles("BASELINE"))
            AutoTarget.SWITCH_ONLY -> ProfileLoader.preloadThese(*profilesSided("SWITCH"))
            AutoTarget.SCALE_ONLY, AutoTarget.FULL -> ProfileLoader.preloadThese(*profilesSided("SCALE"))
        }
    }

    private fun checkSensors(): Boolean {
        if (Drivetrain.imu.state != PigeonIMU.PigeonState.Ready) {
            DriverStation.reportWarning("IMU is not present!  Auto will not run", false)
            return false
        }
        if (Drivetrain.left.encoderMissing()) {
            DriverStation.reportWarning("Left drive encoder is not present!  Auto will not run", false)
            return false
        }
        if (Drivetrain.right.encoderMissing()) {
            DriverStation.reportWarning("Right drive encoder is not present!  Auto will not run", false)
            return false
        }
        return true
    }

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
        if (checkSensors() && target != AutoTarget.NOTHING) {
            Routines.setup() //Run common setup tasks (stow intake, elevator to high gear, lock elevator in place)

            when (target) {
                AutoTarget.BASELINE_ONLY -> {
                    if (robotPos != RobotPosition.DS_CENTER) {
                        Routines.drive(robotPos, FieldElements.baseline(!switchSide), SubSequence(*Commands.HighLockDeployAndWait))
                        add(Commands.HomeElevator)
                    }
                   //AUTO END
                }

                AutoTarget.SWITCH_ONLY -> {
                    Routines.drive(robotPos, FieldElements.switch(switchSide), SubSequence(*Commands.HighLockDeployAndWait)) //Drive and deploy
                    Routines.score() //Score cube
                    add(DelayStep(1000))
                    Routines.drive("BACK_UP")
                    add(Commands.HomeElevator)
                    //AUTO END
                }

                AutoTarget.SCALE_ONLY -> {
                    Routines.drive(robotPos, FieldElements.scale(scaleSide), SubSequence(*Commands.HighLockDeployAndWait, Commands.ScaleAfterUnfold))
                    Routines.score()
                    add(DelayStep(1000))
                    Routines.drive("BACK_UP")
                    add(Commands.HomeElevator)
                    //AUTO END
                }

                AutoTarget.FULL -> {
                    //Drive to scale while deploying and then move carriage to scale position
                    Routines.drive(robotPos, FieldElements.scale(scaleSide), SubSequence(*Commands.HighLockDeployAndWait, Commands.ScaleAfterUnfold))
                    //Score the cube
                    Routines.score()
                    add(DelayStep(1000))
                    Routines.drive("BACK_UP")
                    add(Commands.HomeElevator)
                    /*
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
                    */
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