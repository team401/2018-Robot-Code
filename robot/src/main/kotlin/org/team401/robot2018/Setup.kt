package org.team401.robot2018

import com.ctre.phoenix.motorcontrol.can.TalonSRX
import com.sun.xml.internal.fastinfoset.util.StringArray
import edu.wpi.first.networktables.NetworkTableInstance
import edu.wpi.first.wpilibj.PowerDistributionPanel
import org.snakeskin.annotation.Setup
import org.snakeskin.auto.AutoLoop
import org.snakeskin.auto.AutoManager
import org.snakeskin.registry.*
import org.snakeskin.dsl.Publisher
import org.team401.robot2018.Constants.MJPEGParameters.ADDRESS
import org.team401.robot2018.Constants.MJPEGParameters.PORT
import org.team401.robot2018.auto.motion.MotionProfileRunner
import org.team401.robot2018.subsystems.*
import org.team401.robot2018.vision.VisionController
import org.team401.robot2018.vision.VisionDataClient

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

val visionDataClient = VisionDataClient(ADDRESS, Integer.valueOf(PORT))

val PDP = PowerDistributionPanel()

object TestAuto: AutoLoop() {
    var posLeft by Publisher(0.0)
    var posRight by Publisher(0.0)

    override val rate = 10L
    /*
    lateinit var runnerLeft: RioProfileRunnerOld
    lateinit var runnerRight: RioProfileRunnerOld
    */

    lateinit var runner: MotionProfileRunner

    var started = false

    override fun entry() {
        started = true
        done = false

        /*
        runnerLeft = RioProfileRunnerOld(Drivetrain.left.master)
        runnerRight = RioProfileRunnerOld(Drivetrain.right.master)

        runnerLeft.setPIDFV(f = 0.2)
        runnerRight.setPIDFV(f = 0.2)

        runnerLeft.loadPoints("/home/lvuser/profiles/TUNING_L.csv")
        runnerRight.loadPoints("/home/lvuser/profiles/TUNING_R.csv")

        runnerLeft.reset()
        runnerRight.reset()
        runnerLeft.entry()
        runnerRight.entry()
        */

        runner = MotionProfileRunner(Drivetrain.left.master as TalonSRX, Drivetrain.right.master as TalonSRX)

        runner.reset()
        runner.loadPoints("/home/lvuser/profiles/LEFT_TO_SWITCH_L_.csv", "/home/lvuser/profiles/LEFT_TO_SWITCH_R_.csv")
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

    val mjpeg = StringArray()
    mjpeg.add("mjpeg:https://${Constants.MJPEGParameters.ADDRESS}:${Constants.MJPEGParameters.PORT}/?action=stream")
    NetworkTableInstance.getDefault().getEntry("MJPEG STREAMER").setStringArray(mjpeg.array)

    Subsystems.add(DrivetrainSubsystem)//, ElevatorSubsystem, IntakeSubsystem, RungsSubsystem)
    Controllers.add(LeftStick, RightStick)
    Sensors.add(VisionStopSensor)
    /*
    on(Events.DISABLED) {
        Vision.exit()
        PowerUpAuto.publish()
    }
    */
}