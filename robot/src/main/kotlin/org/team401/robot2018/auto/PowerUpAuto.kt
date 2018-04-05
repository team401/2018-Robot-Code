package org.team401.robot2018.auto

import com.ctre.phoenix.sensors.PigeonIMU
import edu.wpi.first.wpilibj.DriverStation
import org.team401.robot2018.auto.motion.ProfileLoader
import org.team401.robot2018.auto.steps.DelayStep
import org.team401.robot2018.auto.steps.SequentialSteps
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

    override fun assembleAuto(add: StepAdder) {
        add(DelayStep(baseDelay)) //Wait for the base delay
        if (checkSensors() && target != AutoTarget.NOTHING) {
            Routines.setup() //Run common setup tasks (stow intake, elevator to high gear, lock elevator in place)

            when (target) {
                AutoTarget.BASELINE_ONLY -> {
                    if (robotPos != RobotPosition.DS_CENTER) {
                        Routines.drive(robotPos, FieldElements.baseline(!switchSide), SequentialSteps(*Commands.HighLockDeployAndWait))
                        add(Commands.HomeElevator)
                    }
                   //AUTO END
                }

                AutoTarget.SWITCH_ONLY -> {
                    Routines.drive(robotPos, FieldElements.switch(switchSide), SequentialSteps(*Commands.HighLockDeployAndWait)) //Drive and deploy
                    Routines.score() //Score cube
                    Routines.drive("BACK_UP")
                    add(Commands.HomeElevator)
                    //AUTO END
                }

                AutoTarget.SCALE_ONLY -> {
                    if (robotPos.alignedWith(scaleSide)) {
                        Routines.drive(robotPos, FieldElements.scale(scaleSide), SequentialSteps(*Commands.HighLockDeployAndWait, Commands.ScaleAfterUnfold))
                        Routines.score()
                        Routines.drive("BACK_UP")
                    } else {
                        Routines.drive(robotPos, FieldElements.baseline(scaleSide), SequentialSteps(*Commands.HighLockDeployAndWait))
                    }
                    add(Commands.HomeElevator)
                    //AUTO END
                }

                AutoTarget.FULL -> {
                    when (robotPos) {
                        //Center switch
                        RobotPosition.DS_CENTER -> {
                            Routines.drive(robotPos, FieldElements.switch(switchSide), SequentialSteps(*Commands.HighLockDeployAndWait)) //Drive and deploy
                            Routines.score() //Score cube
                            Routines.drive("BACK_UP")
                            add(Commands.HomeElevator)
                        }
                        //Attepmt two cube
                        RobotPosition.DS_LEFT, RobotPosition.DS_RIGHT -> {
                            if (robotPos.alignedWith(scaleSide)) {
                                //Do same side two cube auto
                                Routines.drive(robotPos, FieldElements.scale(scaleSide), SequentialSteps(*Commands.HighLockDeployAndWait, Commands.ScaleAfterUnfold))
                                Routines.score()
                                Routines.drive("BACK_UP")
                                add(Commands.HomeElevator)
                            } else if (robotPos == RobotPosition.DS_CENTER || robotPos.alignedWith(switchSide)) {
                                //Do side switch only
                                Routines.drive(robotPos, FieldElements.switch(switchSide), SequentialSteps(*Commands.HighLockDeployAndWait))
                                Routines.score()
                                Routines.drive("BACK_UP")
                                add(Commands.HomeElevator)
                            } else {
                                //Baseline only
                                Routines.drive(robotPos, FieldElements.baseline(switchSide), SequentialSteps(*Commands.HighLockDeployAndWait))
                                add(Commands.HomeElevator)
                            }
                        }
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