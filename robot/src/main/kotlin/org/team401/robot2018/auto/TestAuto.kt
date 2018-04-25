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
        Routines.drive("DS_LEFT-SCALE_LEFT", SequentialSteps(*Commands.HighLockDeployAndWait(), Commands.ScaleAfterUnfold()))
        Routines.score()
        add(ParallelSteps(ZeroPoint(-105.0, 0.0, .006, 0.0, 0.0, 20.0, 2.0, 1.5),
                SequentialSteps(DelayStep(.1), Commands.HomeElevator()))) //1
        Routines.intake()
        Routines.drive("SCALE_LEFT-SCALE_LEFT_TWOCUBE2")
        add(Commands.WaitForHasCube())
        add(ParallelSteps(ZeroPoint(165.0, 0.0, .006, 0.0, 0.0, 20.0, 2.0, 1.5), Commands.ElevatorToScale()))
        Routines.drive("SCALE_LEFT_TWOCUBE3-SCALE_LEFT")
        Routines.score()
    }
}