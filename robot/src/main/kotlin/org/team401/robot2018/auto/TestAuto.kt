package org.team401.robot2018.auto

import openrio.powerup.MatchData
import org.team401.robot2018.auto.*
import org.team401.robot2018.auto.motion.TuningRioProfileRunner
import org.team401.robot2018.auto.steps.DelayStep
import org.team401.robot2018.auto.steps.LambdaStep
import org.team401.robot2018.auto.steps.SubSequence
import org.team401.robot2018.etc.StepAdder
import org.team401.robot2018.subsystems.Drivetrain
import org.team401.robot2018.subsystems.ElevatorSubsystem
import java.awt.Robot

object TestAuto : RobotAuto(){
    override fun assembleAuto(add: StepAdder) {
        add(TuningRioProfileRunner(Drivetrain.left.master, Drivetrain.right.master, Drivetrain.imu, "memes"))


    }


}