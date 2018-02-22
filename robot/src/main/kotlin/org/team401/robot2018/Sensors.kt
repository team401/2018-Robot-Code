package org.team401.robot2018

import edu.wpi.first.wpilibj.DriverStation
import org.snakeskin.dsl.Sensors
import org.snakeskin.dsl.machine
import org.team401.robot2018.etc.Constants.DrivetrainParameters.PITCH_CORRECTION_MIN
import org.team401.robot2018.etc.Constants.DrivetrainParameters.ROLL_CORRECTION_MIN
import org.team401.robot2018.subsystems.*

/*
 * 2018-Robot-Code - Created on 1/30/18
 * Author: Cameron Earle
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at project root
 */

/**
 * @author Cameron Earle
 * @version 1/30/18
 */

val VisionStopSensor = Sensors.booleanSensor({DriverStation.getInstance().isOperatorControl && DriverStation.getInstance().matchTime <= 5}) {
    pollAt(1000)

    whenTriggered {
        Thread.sleep(10000)
        Vision.stop()
    }
}

/*
val CubeVisionSensor = Sensors.booleanSensor({ VisionData.read().isCubePresent}) {
    pollAt(20)

    whenTriggered {

        IntakeSubsystem.machine(INTAKE_WHEELS_MACHINE).setState(IntakeWheelsStates.INTAKE)
        IntakeSubsystem.machine(INTAKE_FOLDING_MACHINE).setState(IntakeFoldingStates.INTAKE)
    }

    whenUntriggered {

        IntakeSubsystem.machine(INTAKE_WHEELS_MACHINE).setState(IntakeWheelsStates.IDLE)
        if(boxHeld()) IntakeSubsystem.machine(INTAKE_FOLDING_MACHINE).setState(IntakeFoldingStates.STOWED)
        else IntakeSubsystem.machine(INTAKE_FOLDING_MACHINE).setState(IntakeFoldingStates.GRAB)
    }
}
*/

fun getPitch(): Double {
    var imuData = DoubleArray(3)
    Drivetrain.imu.getYawPitchRoll(imuData)
    return imuData[1]
}

fun getRoll(): Double {
    var imuData = DoubleArray(3)
    Drivetrain.imu.getYawPitchRoll(imuData)
    return imuData[2]
}

val PitchSensor = Sensors.numericSensor({Math.abs(getPitch())}) {
    pollAt(20)

    whenAbove(PITCH_CORRECTION_MIN.toDouble()) {

        DrivetrainSubsystem.machine(DRIVE_MACHINE).setState(DriveStates.TIP_CONTROL)
    }

    whenBelow(PITCH_CORRECTION_MIN.toDouble()) {

        DrivetrainSubsystem.machine(DRIVE_MACHINE).setState(DrivetrainSubsystem.machine(DRIVE_MACHINE).getLastState())
    }
}

val RollSensor = Sensors.numericSensor({Math.abs(getRoll())}) {
    pollAt(20)

    whenAbove(ROLL_CORRECTION_MIN.toDouble()) {

        DrivetrainSubsystem.machine(DRIVE_MACHINE).setState(DriveStates.TIP_CONTROL)
    }

    whenBelow(ROLL_CORRECTION_MIN.toDouble()) {

        DrivetrainSubsystem.machine(DRIVE_MACHINE).setState(DrivetrainSubsystem.machine(DRIVE_MACHINE).getLastState())
    }

}