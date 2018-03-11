package org.team401.robot2018

import edu.wpi.first.networktables.NetworkTableInstance
import edu.wpi.first.wpilibj.CameraServer
import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.PowerDistributionPanel
import edu.wpi.first.wpilibj.Servo
import org.snakeskin.annotation.PostStartup
import org.snakeskin.annotation.Setup
import org.snakeskin.auto.AutoManager
import org.snakeskin.dsl.Subsystem
import org.snakeskin.dsl.buildSubsystem
import org.snakeskin.dsl.on
import org.snakeskin.dsl.send
import org.snakeskin.event.Events
import org.snakeskin.registry.Controllers
import org.snakeskin.registry.Sensors
import org.snakeskin.registry.Subsystems
import org.team401.robot2018.auto.PowerUpAuto
//import org.team401.robot2018.auto.TestAuto
import org.team401.robot2018.constants.Constants
import org.team401.robot2018.constants.CompConstants
import org.team401.robot2018.etc.Reporting
import org.team401.robot2018.subsystems.*
import org.team401.robot2018.vision.MjpegServer
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

//val Vision = VisionController("10.4.1.3")
//val VisionData = VisionDataClient(ADDRESS, Integer.valueOf(PORT))
val PDP = PowerDistributionPanel()
//val MJPEG = MjpegServer(1180)

@Setup
fun setup() {
    //Uncomment which one you are using
    Constants = CompConstants()
    //Constants = PracticeConstants()

    //Uncomment which one you are using
    AutoManager.auto = PowerUpAuto //Real auto
    //AutoManager.auto = TestAuto //Test auto

    //Uncomment which one you are using
    PowerUpAuto.publish() //Real auto
    //TestAuto.publish() //Test auto

    //val mjpeg = Array(1) { Constants.Setup.MJPEGParameters.FULL_ADDRESS }
    //NetworkTableInstance.getDefault().getEntry("MJPEG STREAMER").setStringArray(mjpeg)

    CameraServer.getInstance().startAutomaticCapture(0)

    Subsystems.add(DrivetrainSubsystem, ElevatorSubsystem, IntakeSubsystem, RungsSubsystem)
    Controllers.add(LeftStick, RightStick, Gamepad)

    /*
    on(Events.TELEOP_ENABLED) {
        MJPEG.start()
    }
    */
}

//@PostStartup private fun startReporting() = Reporting.start()