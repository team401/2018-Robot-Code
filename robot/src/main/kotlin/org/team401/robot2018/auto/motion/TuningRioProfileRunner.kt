package org.team401.robot2018.auto.motion

import ch.qos.logback.core.status.StatusUtil
import com.ctre.phoenix.motorcontrol.IMotorControllerEnhanced
import com.ctre.phoenix.motorcontrol.StatusFrame
import com.ctre.phoenix.motorcontrol.StatusFrameEnhanced
import com.ctre.phoenix.sensors.PigeonIMU
import com.google.gson.Gson
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import org.snakeskin.component.TankDrivetrain
import org.team401.robot2018.auto.steps.AutoStep
import org.team401.robot2018.etc.RobotMath

/*
 * 2018-Robot-Code - Created on 2/22/18
 * Author: Cameron Earle
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at project root
 */

/**
 * @author Cameron Earle
 * @version 2/22/18
 */

class TuningRioProfileRunner(val drivetrain: TankDrivetrain,
                             val name: String,
                             val rate: Long = 20L): AutoStep() {
    
    private var gains = DriveGains()
    private var driveMagnitude = 0.0
    private var headingMagnitude = 0.0
    private var leftPointfile = ""
    private var rightPointfile = ""

    private lateinit var runner: RioProfileRunner
    private lateinit var leftCurrent: Waypoint
    private lateinit var rightCurrent: Waypoint

    private val gson = Gson()
    
    private fun fetchGains() {
        gains = DriveGains(
                SmartDashboard.getNumber("tuningRunner-$name-P", 0.0),
                SmartDashboard.getNumber("tuningRunner-$name-V", 0.0),
                SmartDashboard.getNumber("tuningRunner-$name-ffV", 0.0),
                SmartDashboard.getNumber("tuningRunner-$name-ffA", 0.0),
                SmartDashboard.getNumber("tuningRunner-$name-H", 0.0)
        )
        driveMagnitude = SmartDashboard.getNumber("tuningRunner-$name-driveMagnitude", 0.0)
        headingMagnitude = SmartDashboard.getNumber("tuningRunner-$name-headingMagnitude", 0.0)

        leftPointfile = SmartDashboard.getString("tuningRunner-$name-lPoints", "")
        rightPointfile = SmartDashboard.getString("tuningRunner-$name-rPoints", "")
    }

    private fun ready() = SmartDashboard.putString("tuningRunner-$name-state", "ready")
    private fun loading() = SmartDashboard.putString("tuningRunner-$name-state", "loading")
    private fun running() = SmartDashboard.putString("tuningRunner-$name-state", "running")

    private data class PublishData(val left: Side,
                                   val right: Side,
                                   val head: Double,
                                   val desHead: Double,
                                   val time: Long) {
        data class Side(val pos: Double,
                        val vel: Double,
                        val desPos: Double,
                        val desVel: Double)
    }
    
    private fun publishData() {
        val imuData = DoubleArray(3)
        drivetrain.imu.getYawPitchRoll(imuData)
        val currentData = PublishData(
            PublishData.Side(
                RobotMath.UnitConversions.nativeUnitsToRevolutions(drivetrain.left.getPosition(0).toDouble()),
                RobotMath.UnitConversions.nativeUnitsToRpm(drivetrain.left.getVelocity(0).toDouble()),
                leftCurrent.position,
                leftCurrent.velocity
            ),
            PublishData.Side(
                    RobotMath.UnitConversions.nativeUnitsToRevolutions(drivetrain.right.getPosition(0).toDouble()),
                    RobotMath.UnitConversions.nativeUnitsToRpm(drivetrain.right.getVelocity(0).toDouble()),
                    rightCurrent.position,
                    rightCurrent.velocity
            ), imuData[0], leftCurrent.heading, leftCurrent.timestep * runner.index().toLong()
        )

        SmartDashboard.putString("tuningRunner-$name-current", gson.toJson(currentData))
    }
    
    override fun entry() {
        done = false
        SmartDashboard.putString("tuningRunner-$name-current", "{}")
        loading()
        fetchGains()
        runner = RioProfileRunner(drivetrain, gains, headingMagnitude, driveMagnitude, rate)
        runner.loadPoints(leftPointfile, rightPointfile)
        runner.entry()
    }
    
    override fun action() {
        running()
        runner.action()
        leftCurrent = runner.leftCurrentWaypoint()
        rightCurrent = runner.rightCurrentWaypoint()
        publishData()
        done = runner.done
    }
    
    override fun exit() {
        runner.exit()
        ready()
    }
}