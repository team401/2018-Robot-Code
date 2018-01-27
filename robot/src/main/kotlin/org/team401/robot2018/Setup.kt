package org.team401.robot2018

import com.ctre.phoenix.motion.MotionProfileStatus
import com.ctre.phoenix.motion.SetValueMotionProfile
import com.ctre.phoenix.motion.TrajectoryPoint
import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.IMotorControllerEnhanced
import com.ctre.phoenix.motorcontrol.can.TalonSRX
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import org.snakeskin.auto.AutoLoop
import org.snakeskin.auto.AutoManager
import org.snakeskin.dsl.*
import org.snakeskin.event.Events
import org.snakeskin.registry.*
import org.team401.robot2018.auto.MotionProfileRunner
import org.team401.robot2018.auto.MotionProfileRunner2
import org.team401.robot2018.auto.PowerUpAuto
import org.team401.robot2018.subsystems.*
import org.team401.robot2018.vision.VisionController
import java.io.File

/*
 * 2018-Robot-Code - Created on 1/5/18
 * Author: Cameron Earle
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at project root
 */

/**
 * @author Cameron Earle
 * @version 1/5/18
 */

val Vision = VisionController("10.4.1.3")

object TestAuto: AutoLoop() {
    var pos by Publisher(0.0)

    override val rate = 10L

    lateinit var runner: MotionProfileRunner2

    var started = false

    override fun entry() {
        started = true
        done = false
        runner = MotionProfileRunner2(Drivetrain.left.master)

        runner.loadPoints("/home/lvuser/TUNING_L.csv")
        runner.entry()
    }

    override fun action() {
        runner.action()
        pos = Drivetrain.left.getPosition(0) / 4096.0

        if (runner.done) {
            done = true
        }
    }

    override fun exit() {
        if (started) {
            runner.exit()
        }
    }
}

@Setup fun setup() {
    //AutoManager.auto = PowerUpAuto
    AutoManager.auto = TestAuto

    //PowerUpAuto.publish()

    Subsystems.add(DrivetrainSubsystem)//, ElevatorSubsystem, IntakeSubsystem, RungsSubsystem)
    Controllers.add(LeftStick, RightStick)
    /*
    on(Events.DISABLED) {
        Vision.exit()
        PowerUpAuto.publish()
    }
    */
}