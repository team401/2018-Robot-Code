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
import org.team401.robot2018.auto.motion.PDVA
import org.team401.robot2018.auto.motion.RioProfileRunner
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

val VisionData = VisionDataClient(ADDRESS, Integer.valueOf(PORT))

val PDP = PowerDistributionPanel()

object TestAuto: AutoLoop() {
    var posLeft by Publisher(0.0)
    var posRight by Publisher(0.0)

    override val rate = 10L

    lateinit var runner: RioProfileRunner

    var started = false

    override fun entry() {
        done = false
        started = true
        runner = RioProfileRunner(Drivetrain.left.master, Drivetrain.right.master, Drivetrain.imu,
                PDVA(p = 0.1, v = 1/1250.0),
                PDVA(p = 0.1, v = 1/1250.0),
                0.015,
                tuning = true)

        runner.loadPoints("/home/lvuser/profiles/LEFT_TO_SWITCH_L.csv", "/home/lvuser/profiles/LEFT_TO_SWITCH_R.csv")
        runner.entry()
    }

    override fun action() {
        runner.action()

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

    val mjpeg = StringArray()
    mjpeg.add("mjpeg:https://${Constants.MJPEGParameters.ADDRESS}:${Constants.MJPEGParameters.PORT}/?action=stream")
    NetworkTableInstance.getDefault().getEntry("MJPEG STREAMER").setStringArray(mjpeg.array)

    Subsystems.add(DrivetrainSubsystem, IntakeSubsystem)
    Controllers.add(LeftStick, RightStick)
    Sensors.add(VisionStopSensor)
}