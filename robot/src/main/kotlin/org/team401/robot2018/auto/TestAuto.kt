package org.team401.robot2018.auto

import org.team401.robot2018.auto.motion.ProfileLoader
import org.team401.robot2018.auto.motion.TuningRioProfileRunner
import org.team401.robot2018.auto.motionprofile.ArcProfileFollower
import org.team401.robot2018.etc.StepAdder
import org.team401.robot2018.subsystems.Drivetrain

object TestAuto : RobotAuto() {
    override fun preAuto() {
        ProfileLoader.preload("/home/lvuser/profiles/DS_CENTER-SCALE_LEFT_L.csv")
        ProfileLoader.preload("/home/lvuser/profiles/DS_CENTER-SCALE_LEFT_R.csv")
    }

    override fun assembleAuto(add: StepAdder) {
        add(Commands.ZeroIMU)
        add(ArcProfileFollower(Drivetrain).apply {
            load("/home/lvuser/profiles/ARC_TEST_C.csv")
        })
        //Routines.drive("DS_LEFT", "SCALE_LEFT")
        //Routines.drive("SCALE_LEFT", "SCALE_OFFSET_LEFT")
        //Routines.drive("SCALE_OFFSET_LEFT", "SWITCH_LEFT")
        //add(TuningRioProfileRunner(Drivetrain.left.master, Drivetrain.right.master, Drivetrain.imu, "testing"))
    }
}