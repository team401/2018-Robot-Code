package org.team401.robot2018

import com.ctre.phoenix.motion.MotionProfileStatus
import com.ctre.phoenix.motion.SetValueMotionProfile
import com.ctre.phoenix.motion.TrajectoryPoint
import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.IMotorControllerEnhanced
import com.ctre.phoenix.motorcontrol.can.TalonSRX
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import org.snakeskin.auto.TempAutoManager
import org.snakeskin.dsl.*
import org.snakeskin.event.Events
import org.snakeskin.registry.*
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

@Setup fun setup() {
    Subsystems.add(DrivetrainSubsystem, ElevatorSubsystem, IntakeSubsystem, RungsSubsystem)
    Controllers.add(LeftStick, RightStick, MasherBox)

    on(Events.DISABLED) {
        Vision.stop()
    }

    TempAutoManager.auto = autoLoop {}
}