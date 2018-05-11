package org.team401.robot2018.auto.motionprofile

import com.google.gson.Gson
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import org.snakeskin.component.TankDrivetrain
import org.team401.robot2018.auto.steps.AutoStep
import org.team401.robot2018.etc.RobotMath

class TuningArcProfileFollower(drivetrain: TankDrivetrain, val name: String): ArcProfileFollower(drivetrain) {
    private var timer = 0L

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

    val gson = Gson()

    private fun publishData() {
        val imuData = DoubleArray(3)
        drivetrain.imu.getYawPitchRoll(imuData)
        val currentData = PublishData(
                PublishData.Side(
                        RobotMath.Drivetrain.nativeUnitsToInches(controller.getSelectedSensorPosition(0).toDouble()),
                        RobotMath.Drivetrain.nativeUnitsToIps(controller.getSelectedSensorVelocity(0).toDouble()),
                        RobotMath.Drivetrain.nativeUnitsToInches(controller.activeTrajectoryPosition.toDouble()),
                        RobotMath.Drivetrain.nativeUnitsToIps(controller.activeTrajectoryVelocity.toDouble())
                ),
                PublishData.Side(
                        0.0,
                        0.0,
                        0.0,
                        0.0
                ), imuData[0], controller.activeTrajectoryHeading / 10.0, timer
        )

        SmartDashboard.putString("tuningRunner-$name-current", gson.toJson(currentData))
    }

    override fun entry(currentTime: Double) {
        timer = 0
        SmartDashboard.putString("tuningRunner-$name-current", "{}")
        loading()
        super.entry(currentTime)
    }

    override fun action(currentTime: Double, lastTime: Double) {
        super.action(currentTime, lastTime)
        running()
        timer += ((currentTime - lastTime) * 1000).toLong()
        publishData()
    }

    override fun exit(currentTime: Double) {
        ready()
        super.exit(currentTime)
    }
}