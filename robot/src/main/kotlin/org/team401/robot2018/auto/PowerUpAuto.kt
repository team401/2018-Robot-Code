package org.team401.robot2018.auto

import com.ctre.phoenix.sensors.PigeonIMU
import edu.wpi.first.wpilibj.DriverStation
import org.team401.robot2018.auto.motionprofile.ProfileLoader
import org.team401.robot2018.auto.steps.DelayStep
import org.team401.robot2018.auto.steps.SequentialSteps
import org.team401.robot2018.etc.StepAdder
import org.team401.robot2018.etc.encoderMissing
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
        //Here, we identify all possible first profiles and cache them
        //This is done using the selected mode
        ProfileLoader.preloadThese(target.firstProfiles.map { "/home/lvuser/profiles/${it}_C.csv" })
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
            Routines.setup() //Run common setup tasks (reset elevator home, reset heading, home intake)
            when(target) {
                //Two cube switch auto from center position
                AutoTarget.CENTER_SWITCH -> {
                    //Drive to switch while deploying
                    Routines.drive(robotPos, FieldElements.switch(switchSide), SequentialSteps(*Commands.HighLockDeployAndWait()))
                    //Score cube
                    Routines.score()
                    //Back up from switch while homing elevator
                    Routines.drive(FieldElements.switch(switchSide), FieldElements.switch(switchSide, 1), Commands.HomeElevator())
                    //Open intake
                    Routines.intake()
                    //Drive to second cube
                    Routines.drive(FieldElements.switch(switchSide, 1), FieldElements.switch(switchSide, 2))
                    //Wait for cube
                    add(Commands.WaitForHasCube())
                    //Back up from cube, raise elevator
                    Routines.drive(FieldElements.switch(switchSide, 2), FieldElements.switch(switchSide, 3), Commands.ElevatorToSwitch())
                    //Wait for elevator at switch
                    add(Commands.WaitForAtSwitch())
                    //Drive to switch
                    Routines.drive(FieldElements.switch(switchSide, 3), FieldElements.switch(switchSide))
                    //Score cube
                    Routines.score()
                    //Back up
                    Routines.drive("BACK_UP")
                }

                //One cube near switch auto or baseline from left or right position
                AutoTarget.SWITCH_LEFT, AutoTarget.SWITCH_RIGHT -> {
                    if (robotPos alignedWith switchSide) {
                        //Drive to switch while deploying
                        Routines.drive(robotPos, FieldElements.switch(switchSide), SequentialSteps(*Commands.HighLockDeployAndWait()))
                        //Score cube
                        Routines.score()
                        //Back up
                        Routines.drive("BACK_UP")
                        //Home elevator
                        add(Commands.HomeElevator())
                    } else {
                        //Drive to baseline while deploying
                        Routines.drive(FieldElements.baseline(), SequentialSteps(*Commands.HighLockDeployAndWait()))
                        //Home elevator
                        add(Commands.HomeElevator())
                    }
                }

                //Two cube scale auto near or one cube scale auto far from left or right position
                AutoTarget.FULL_SCALE_LEFT, AutoTarget.FULL_SCALE_RIGHT -> {
                    //Drive to scale while deploying
                    Routines.drive(robotPos, FieldElements.scale(scaleSide), SequentialSteps(*Commands.HighLockDeployAndWait()))
                    //Score cube
                    Routines.score()
                    //Check side
                    if (robotPos alignedWith scaleSide) {
                        //TODO replace drive steps with zero turns
                        //TODO actually add drive steps
                    } else {
                        //Back up
                        Routines.drive("BACK_UP")
                        //Home elevator
                        add(Commands.HomeElevator())
                    }
                }

                //Two cube scale auto near or one cube switch auto near or baseline from left or right position
                AutoTarget.STAY_NEAR_LEFT, AutoTarget.STAY_NEAR_RIGHT -> {
                    //Check target
                    when {
                        //Go to scale
                        robotPos alignedWith scaleSide -> {
                            //TODO full scale
                        }
                        //Go to switch
                        robotPos alignedWith switchSide -> {
                            //Drive to switch while deploying
                            Routines.drive(robotPos, FieldElements.switch(switchSide), SequentialSteps(*Commands.HighLockDeployAndWait()))
                            //Score cube
                            Routines.score()
                            //Back up
                            Routines.drive("BACK_UP")
                            //Home elevator
                            add(Commands.HomeElevator())
                        }
                        //Go to baseline
                        else -> {
                            //Drive to baseline while deploying elevator
                            Routines.drive(FieldElements.baseline(), SequentialSteps(*Commands.HighLockDeployAndWait()))
                            //Home elevator
                            add(Commands.HomeElevator())
                        }
                    }
                }
            }
        }
    }
}