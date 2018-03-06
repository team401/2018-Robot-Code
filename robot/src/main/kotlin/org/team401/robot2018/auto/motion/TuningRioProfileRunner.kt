package org.team401.robot2018.auto.motion

import com.ctre.phoenix.motorcontrol.IMotorControllerEnhanced
import com.ctre.phoenix.sensors.PigeonIMU
import com.google.gson.Gson
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
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

class TuningRioProfileRunner(override val leftController: IMotorControllerEnhanced,
                             override val rightController: IMotorControllerEnhanced, 
                             val imu: PigeonIMU,
                             val name: String,
                             val rate: Long = 20L): TankMotionStep() {
    
    private var leftGains = PDVA()
    private var rightGains = PDVA()
    private var headingGain = 0.0
    private var leftPointfile = ""
    private var rightPointfile = ""

    private lateinit var runner: RioProfileRunner
    private lateinit var leftCurrent: RioProfileRunner.Waypoint
    private lateinit var rightCurrent: RioProfileRunner.Waypoint

    private val gson = Gson()
    
    private fun fetchGains() {
        leftGains = PDVA(
                SmartDashboard.getNumber("tuningRunner-$name-leftP", 0.0),
                SmartDashboard.getNumber("tuningRunner-$name-leftD", 0.0),
                SmartDashboard.getNumber("tuningRunner-$name-leftV", 0.0),
                SmartDashboard.getNumber("tuningRunner-$name-leftA", 0.0)
        )
        rightGains = PDVA(
                SmartDashboard.getNumber("tuningRunner-$name-rightP", 0.0),
                SmartDashboard.getNumber("tuningRunner-$name-rightD", 0.0),
                SmartDashboard.getNumber("tuningRunner-$name-rightV", 0.0),
                SmartDashboard.getNumber("tuningRunner-$name-rightA", 0.0)
        )
        headingGain = SmartDashboard.getNumber("tuningRunner-$name-H", 0.0)

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
        data class Side(val pos: Int,
                        val vel: Int,
                        val desPos: Double,
                        val desVel: Double)
    }
    
    private fun publishData() {
        val imuData = DoubleArray(3)
        imu.getYawPitchRoll(imuData)
        val currentData = PublishData(
            PublishData.Side(
                RobotMath.UnitConversions.nativeUnitsToRevolutions(leftController.getSelectedSensorPosition(0).toDouble()).toInt(),
                RobotMath.UnitConversions.nativeUnitsToRpm(leftController.getSelectedSensorVelocity(0).toDouble()).toInt(),
                leftCurrent.position,
                leftCurrent.velocity
            ),
            PublishData.Side(
                    RobotMath.UnitConversions.nativeUnitsToRevolutions(rightController.getSelectedSensorPosition(0).toDouble()).toInt(),
                    RobotMath.UnitConversions.nativeUnitsToRpm(rightController.getSelectedSensorVelocity(0).toDouble()).toInt(),
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
        runner = RioProfileRunner(leftController, rightController, imu, leftGains, rightGains, headingGain, rate)
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