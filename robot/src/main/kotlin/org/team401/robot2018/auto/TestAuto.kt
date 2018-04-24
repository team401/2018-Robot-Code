package org.team401.robot2018.auto

import com.ctre.phoenix.motorcontrol.FeedbackDevice
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import org.team401.robot2018.auto.motionprofile.ProfileLoader
import org.team401.robot2018.auto.motionprofile.ArcProfileFollower
import org.team401.robot2018.auto.motionprofile.TuningArcProfileFollower
import org.team401.robot2018.auto.motionprofile.ZeroPoint
import org.team401.robot2018.auto.steps.*
import org.team401.robot2018.etc.StepAdder
import org.team401.robot2018.subsystems.Drivetrain

object TestAuto : RobotAuto() {
    override fun preAuto() {
    }

    override fun assembleAuto(add: StepAdder) {
        Routines.setup()
        add(ZeroPoint(-90.0, SmartDashboard.getNumber("ztkF", 0.0),
                SmartDashboard.getNumber("ztkP", 0.0),
                SmartDashboard.getNumber("ztkD", 0.0),
                2.0))
        /*
        Routines.setup()
        Routines.drive("TWOSWITCH_1", *Commands.HighLockDeployAndWait())
        Routines.score()
        Routines.drive("TWOSWITCH_2", Commands.HomeElevator())
        Routines.intake()
        Routines.drive("TWOSWITCH_3")
        add(Commands.WaitForHasCube())
        Routines.drive("TWOSWITCH_4", Commands.ElevatorToSwitch())
        add(Commands.WaitForAtSwitch())
        Routines.drive("TWOSWITCH_5")
        Routines.score()
        Routines.drive("BACK_UP")
*/
/*
        Routines.setup()
        Routines.drive("TWOSCALE_1", SequentialSteps(*Commands.HighLockDeployAndWait(), Commands.ScaleAfterUnfold()))
        add(Commands.PrintTime("TWOSCALE_1"))
        Routines.score()
        add(Commands.PrintTime("SCORE"))
        Routines.drive("TWOSCALE_2", SequentialSteps(DelayStep(.25), Commands.HomeElevator()))
        add(Commands.PrintTime("TWOSCALE_2, ELEVATOR HOME"))
        Routines.intake()
        add(Commands.PrintTime("INTAKE"))
        Routines.drive("TWOSCALE_3")
        add(Commands.PrintTime("TWOSCALE_3"))
        add(Commands.WaitForHasCube())
        add(Commands.PrintTime("CUBE"))
        Routines.drive("TWOSCALE_4", Commands.ElevatorToScale())
        add(Commands.PrintTime("TWOSCALE_4, ELEVATOR SCALE"))
        Routines.drive("TWOSCALE_5")
        add(Commands.PrintTime("TWOSCALE_5"))
        Routines.score()
        add(Commands.PrintTime("SCORE"))
        Routines.drive("BACK_UP")
        add(Commands.PrintTime("BACK_UP"))
        */
    }
}