package org.team401.robot2018

import com.ctre.phoenix.motion.SetValueMotionProfile
import com.ctre.phoenix.motorcontrol.can.TalonSRX
import edu.wpi.first.wpilibj.Talon
import org.snakeskin.auto.AutoLoop
import org.snakeskin.auto.AutoManager
import org.snakeskin.dsl.*
import org.snakeskin.registry.*
import org.team401.robot2018.auto.MotionProfileRunner2
import org.team401.robot2018.auto.MotionProfileRunner3
import org.team401.robot2018.auto.RioProfileRunner
import org.team401.robot2018.subsystems.*
import org.team401.robot2018.vision.VisionController

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
    var posLeft by Publisher(0.0)
    var posRight by Publisher(0.0)

    override val rate = 10L
    /*
    lateinit var runnerLeft: RioProfileRunner
    lateinit var runnerRight: RioProfileRunner
    */

    lateinit var runner: MotionProfileRunner3

    var started = false

    override fun entry() {
        started = true
        done = false

        /*
        runnerLeft = RioProfileRunner(Drivetrain.left.master)
        runnerRight = RioProfileRunner(Drivetrain.right.master)

        runnerLeft.setPIDFV(f = 0.2)
        runnerRight.setPIDFV(f = 0.2)

        runnerLeft.loadPoints("/home/lvuser/profiles/TUNING_L.csv")
        runnerRight.loadPoints("/home/lvuser/profiles/TUNING_R.csv")

        runnerLeft.reset()
        runnerRight.reset()
        runnerLeft.entry()
        runnerRight.entry()
        */

        runner = MotionProfileRunner3(Drivetrain.left.master as TalonSRX, Drivetrain.right.master as TalonSRX)

        runner.reset()
        runner.loadPoints("/home/lvuser/profiles/TUNING_L.csv", "/home/lvuser/profiles/TUNING_R.csv")
        runner.entry()

    }

    override fun action() {
        /*
        runnerLeft.action()
        runnerRight.action()

        //posLeft = Drivetrain.left.getPosition(0) / 4096.0
        //posRight = Drivetrain.right.getPosition(0) / 4096.0

        if (runnerLeft.done && runnerRight.done) {
            done = true
        }
        */

        runner.action()

        posLeft = Drivetrain.left.getPosition(0) / 4096.0
        posRight = Drivetrain.right.getPosition(0) / 4096.0

        if (runner.done) {
            done = true
        }
    }

    override fun exit() {
        if (started) {
            /*
            runnerLeft.exit()
            runnerRight.exit()
            */
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