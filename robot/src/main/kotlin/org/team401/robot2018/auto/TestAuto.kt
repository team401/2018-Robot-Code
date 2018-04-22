package org.team401.robot2018.auto

import com.ctre.phoenix.motorcontrol.FeedbackDevice
import org.team401.robot2018.auto.motionprofile.ProfileLoader
import org.team401.robot2018.auto.motionprofile.ArcProfileFollower
import org.team401.robot2018.auto.motionprofile.TuningArcProfileFollower
import org.team401.robot2018.auto.steps.AutoStep
import org.team401.robot2018.auto.steps.LambdaStep
import org.team401.robot2018.etc.StepAdder
import org.team401.robot2018.subsystems.Drivetrain

object TestAuto : RobotAuto() {
    override fun preAuto() {
    }

    override fun assembleAuto(add: StepAdder) {
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
    }
}