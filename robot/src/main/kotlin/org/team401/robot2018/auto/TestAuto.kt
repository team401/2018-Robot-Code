package org.team401.robot2018.auto

import org.team401.robot2018.auto.motionprofile.ProfileLoader
import org.team401.robot2018.auto.motionprofile.ArcProfileFollower
import org.team401.robot2018.auto.steps.AutoStep
import org.team401.robot2018.etc.StepAdder
import org.team401.robot2018.subsystems.Drivetrain

object TestAuto : RobotAuto() {
    override fun preAuto() {
    }

    override fun assembleAuto(add: StepAdder) {
        add(Commands.ZeroIMU)
        add(ArcProfileFollower(Drivetrain).apply {
            load("/home/lvuser/profiles/ARC_TESTING_C.csv")
        })

        /*
        add(object: AutoStep() {
            override fun entry(currentTime: Double) {
                println("ENTRY")
            }

            override fun action(currentTime: Double, lastTime: Double) {
                println("ACTION: ${(currentTime - lastTime) * 1000}")
            }

            override fun exit(currentTime: Double) {
                println("EXIT")
            }
        })
        */
        //Routines.drive("DS_LEFT", "SCALE_LEFT")
        //Routines.drive("SCALE_LEFT", "SCALE_OFFSET_LEFT")
        //Routines.drive("SCALE_OFFSET_LEFT", "SWITCH_LEFT")
        //add(TuningRioProfileRunner(Drivetrain.left.master, Drivetrain.right.master, Drivetrain.imu, "testing"))
    }
}