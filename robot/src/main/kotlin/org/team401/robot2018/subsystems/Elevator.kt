package org.team401.robot2018.subsystems

import com.ctre.phoenix.motorcontrol.*
import com.ctre.phoenix.motorcontrol.can.TalonSRX
import com.ctre.phoenix.motorcontrol.can.VictorSPX
import edu.wpi.first.wpilibj.Solenoid
import org.snakeskin.component.Gearbox
import org.snakeskin.dsl.Subsystem
import org.snakeskin.dsl.buildSubsystem
import org.snakeskin.event.Events
import org.snakeskin.logic.LockingDelegate
import org.snakeskin.publish.Publisher
import org.team401.robot2018.Gamepad
import org.team401.robot2018.PDP
import org.team401.robot2018.etc.*

//import org.team401.robot2018.MasherBox

/*
 * 2018-Robot-Code - Created on 1/15/18
 * Author: Cameron Earle
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at project root
 */

/**
 * @author Cameron Earle
 * @version 1/15/18
 */

val ELEVATOR_DEPLOY_MACHINE = "elevator_deploy"
object ElevatorDeployStates {
    const val STOWED = "stowed"
    const val DEPLOY = "deploy"
    const val DEPLOYED = "deployed"
}

val ELEVATOR_MACHINE = "elevator"
object ElevatorStates {
    const val OPEN_LOOP_CONTROL = "openloop"
    const val MANUAL_ADJUSTMENT = "closedloop"
    const val HOLD_POS_UNKNOWN = "pos_lock"
    const val HOMING = "homing"

    const val GO_TO_DRIVE = "goToDrive"
    const val GO_TO_COLLECTION = "goToCollection"

    const val POS_COLLECTION = "ground"
    const val POS_DRIVE = "hold"
    const val POS_SWITCH = "switch"
    const val POS_SCALE_LOW = "scale_low"
    const val POS_SCALE = "scale"
    const val POS_SCALE_HIGH = "scale_high"
    const val POS_MAX = "max"
}

val ELEVATOR_SHIFTER_MACHINE = "elevator_shifter"
object ElevatorShifterStates {
    const val RUN = "high"
    const val CLIMB = "low"
    const val HOLD_CARRIAGE = "hold_carriage"
}

val ELEVATOR_RATCHET_MACHINE = "elevator_ratchet"
object ElevatorRatchetStates {
    const val LOCKED = "locked"
    const val UNLOCKED = "unlocked"
}

val ELEVATOR_KICKER_MACHINE = "elevator_kicker"
object ElevatorKickerStates {
    const val KICK = "out"
    const val STOW = "in"
}

val ELEVATOR_CLAMP_MACHINE = "elevator_clamp"
object  ElevatorClampStates{
    const val CLAMPED = "out"
    const val UNCLAMPED = "in"
}

object Elevator {
    lateinit var gearbox: Gearbox
    lateinit var shifter: Solenoid
    lateinit var deployer: Solenoid
    lateinit var ratchet: Solenoid
    lateinit var kicker: Solenoid
    lateinit var clamp: Solenoid

    var homed by LockingDelegate(false)

    fun atCollection() = gearbox.master.getSelectedSensorPosition(0).toDouble().withinTolerance(Constants.ElevatorParameters.COLLECTION_POS, 1000.0)
}

val ElevatorSubsystem: Subsystem = buildSubsystem {
    val master = TalonSRX(Constants.MotorControllers.ELEVATOR_MASTER_CAN)
    val slave1 = VictorSPX(Constants.MotorControllers.ELEVATOR_SLAVE_1_CAN)
    val slave2 = VictorSPX(Constants.MotorControllers.ELEVATOR_SLAVE_2_CAN)
    val slave3 = VictorSPX(Constants.MotorControllers.ELEVATOR_SLAVE_3_CAN)

    val gearbox = Gearbox(master, slave1, slave2, slave3)

    gearbox.setInverted(true)
    master.setSensorPhase(true)

    val shifter = Solenoid(Constants.Pneumatics.ELEVATOR_SHIFTER_SOLENOID)
    val deployer = Solenoid(Constants.Pneumatics.ELEVATOR_DEPLOY_SOLENOID)
    val ratchet = Solenoid(Constants.Pneumatics.ELEVATOR_RATCHET_SOLENOID)
    val kicker = Solenoid(Constants.Pneumatics.ELEVATOR_KICKER_SOLENOID)
    val clamp = Solenoid(Constants.Pneumatics.ELEVATOR_CLAMP_SOLENOID)

    setup {
        Elevator.gearbox = gearbox
        Elevator.shifter = shifter
        Elevator.deployer = deployer
        Elevator.ratchet = ratchet
        Elevator.kicker = kicker
        Elevator.clamp = clamp
        
        gearbox.setSensor(FeedbackDevice.CTRE_MagEncoder_Absolute)

        master.configMotionCruiseVelocity(21680, 0)
        master.configMotionAcceleration(21680, 0)
        //master.pidf(Constants.ElevatorParameters.PIDF)
        gearbox.setCurrentLimit(Constants.ElevatorParameters.CURRENT_LIMIT_CONTINUOUS)
        master.configReverseLimitSwitchSource(LimitSwitchSource.FeedbackConnector, LimitSwitchNormal.NormallyOpen, 10)

        master.setSelectedSensorPosition(0, 0, 0)

        master.configPeakOutputForward(1.0, 0)
        master.configPeakOutputReverse(-1.0, 0)

    }

    val elevatorDeployMachine = stateMachine(ELEVATOR_DEPLOY_MACHINE) {

        state(ElevatorDeployStates.STOWED) {
            entry {
                deployer.set(Constants.ElevatorParameters.DeployMachine.LOCKED)
            }
        }

        state(ElevatorDeployStates.DEPLOY) {
            timeout(Constants.ElevatorParameters.DEPLOY_TIMER, ElevatorDeployStates.DEPLOYED)

            entry {
                deployer.set(Constants.ElevatorParameters.DeployMachine.UNLOCKED)
            }
        }

        state(ElevatorDeployStates.DEPLOYED) {
            entry {
                deployer.set(Constants.ElevatorParameters.DeployMachine.LOCKED)
            }
        }

        default {
            entry {
                deployer.set(false)
            }
        }
    }

    fun notDeployed() = elevatorDeployMachine.getState() != ElevatorDeployStates.DEPLOYED

    val elevatorMachine = stateMachine(ELEVATOR_MACHINE) {
        fun posSetpoint(setpoint: Number) = gearbox.set(ControlMode.Position, setpoint.toDouble())
        fun mmSetpoint(setpoint: Number) = gearbox.set(ControlMode.MotionMagic, setpoint.toDouble())

        state(ElevatorStates.OPEN_LOOP_CONTROL) {
            rejectIf { elevatorDeployMachine.getState() != ElevatorDeployStates.DEPLOYED }

            action {
                gearbox.set(ControlMode.PercentOutput, 0.0)
                println("OPENLOOP: " + master.getSelectedSensorPosition(0))
            }
        }

        state(ElevatorStates.GO_TO_DRIVE) {
            action {
                if (Intake.stowed() || Intake.atGrab()) {
                    setState(ElevatorStates.POS_DRIVE)
                }
            }
        }

        state(ElevatorStates.GO_TO_COLLECTION) {
            action {
                if (Intake.stowed() || Intake.atGrab()) {
                    setState(ElevatorStates.POS_COLLECTION)
                }
            }
        }

        state(ElevatorStates.POS_COLLECTION) {
            rejectIf (::notDeployed)

            entry {
                mmSetpoint(Constants.ElevatorParameters.COLLECTION_POS)
            }
        }

        state(ElevatorStates.POS_DRIVE) {
            rejectIf (::notDeployed)

            entry {
                mmSetpoint(Constants.ElevatorParameters.CUBE_POS)
            }
            action {
                println("UP: " + master.getSelectedSensorPosition(0))
            }
        }

        state(ElevatorStates.POS_SWITCH) {
            rejectIf (::notDeployed)

            entry {
                mmSetpoint(Constants.ElevatorParameters.SWITCH_POS)
            }

            action {
                println("DOWN: " + master.getSelectedSensorPosition(0))
            }
        }

        state(ElevatorStates.POS_SCALE_LOW) {
            rejectIf (::notDeployed)

            entry {
                mmSetpoint(Constants.ElevatorParameters.SCALE_POS_LOW)
            }
        }

        state(ElevatorStates.POS_SCALE) {
            rejectIf (::notDeployed)

            entry {
                mmSetpoint(Constants.ElevatorParameters.SCALE_POS)
            }

            action {
                println("SCALE: " + master.getSelectedSensorPosition(0))

            }
        }

        state (ElevatorStates.POS_SCALE_HIGH) {
            rejectIf (::notDeployed)

            entry {
                mmSetpoint(Constants.ElevatorParameters.SCALE_POS_HIGH)
            }
        }

        state (ElevatorStates.POS_MAX) {
            rejectIf (::notDeployed)

            entry {
                mmSetpoint(Constants.ElevatorParameters.MAX_POS)
            }
        }

        state(ElevatorStates.HOLD_POS_UNKNOWN) {
            entry {
                gearbox.setPosition(0)
            }

            action {
                gearbox.set(ControlMode.Position, 0.0)
            }
        }

        state(ElevatorStates.HOMING) {
            rejectIf { elevatorDeployMachine.getState() != ElevatorDeployStates.DEPLOYED }

            var homingCounter = 0
            var current: Double

            entry {
                Elevator.homed = false
                homingCounter = 0
            }

            action {
                current = gearbox.getCurrent(
                        Constants.PDPChannels.ELEVATOR_SLAVE_1_PDP,
                        Constants.PDPChannels.ELEVATOR_SLAVE_2_PDP,
                        Constants.PDPChannels.ELEVATOR_SLAVE_3_PDP
                )

                gearbox.set(ControlMode.PercentOutput, Constants.ElevatorParameters.HOMING_RATE) //Run down at the homing rate

                if (current > Constants.ElevatorParameters.HOMING_CURRENT) {
                    homingCounter++
                } else {
                    homingCounter = 0
                }

                if (homingCounter > Constants.ElevatorParameters.HOMING_COUNT) {
                    gearbox.stop()
                    gearbox.setPosition(0)
                    Elevator.homed = true
                    setState(ElevatorStates.POS_COLLECTION)

                }
            }
        }

        state(ElevatorStates.MANUAL_ADJUSTMENT) {
            var position = 0

            entry {
                position = gearbox.getPosition()
            }

            action {
                position += RobotMath.Elevator.inchesToTicks((Gamepad.readAxis { LEFT_Y } * Constants.ElevatorParameters.MANUAL_RATE)).toInt()
                if (position > Constants.ElevatorParameters.MAX_POS) position = Constants.ElevatorParameters.MAX_POS.toInt()
                if (position < Constants.ElevatorParameters.ZERO_POS) position = Constants.ElevatorParameters.ZERO_POS.toInt()
                posSetpoint(position)
            }
        }

        default {
            action {
                println("RESTING: " + master.getSelectedSensorPosition(0))
            }
        }
    }

    val elevatorShifterMachine = stateMachine(ELEVATOR_SHIFTER_MACHINE) {
        state(ElevatorShifterStates.RUN) {
            entry {
                shifter.set(Constants.ElevatorParameters.ShifterMachine.HIGH)
            }
        }

        state(ElevatorShifterStates.CLIMB) {
            entry {
                shifter.set(Constants.ElevatorParameters.ShifterMachine.LOW)
            }
        }

        state(ElevatorShifterStates.HOLD_CARRIAGE) {
            entry {
                shifter.set(Constants.ElevatorParameters.ShifterMachine.HOLD)
            }
        }

        default {
            entry {
                shifter.set(false)
            }
        }
    }

    val elevatorRatchetMachine = stateMachine(ELEVATOR_RATCHET_MACHINE) {

        state(ElevatorRatchetStates.LOCKED) {
            entry {
                ratchet.set(Constants.ElevatorParameters.RachetMachine.LOCKED)
            }
        }

        state(ElevatorRatchetStates.UNLOCKED) {
            entry {
                ratchet.set(Constants.ElevatorParameters.RachetMachine.UNLOCKED)
            }
        }

        default {
            entry {
                ratchet.set(false)
            }
        }
    }

    val elevatorKickerMachine = stateMachine(ELEVATOR_KICKER_MACHINE) {

        state(ElevatorKickerStates.KICK) {
            entry {
                kicker.set(Constants.ElevatorParameters.KickerMachine.EXTENDED)
            }
        }

        state(ElevatorKickerStates.STOW) {
            entry {
                kicker.set(Constants.ElevatorParameters.KickerMachine.RETRACTED)
            }
        }

        default {
            entry {
                kicker.set(false)
            }
        }
    }

    val elevatorClampMachine = stateMachine(ELEVATOR_CLAMP_MACHINE) {
        state(ElevatorClampStates.CLAMPED){
            entry{
                clamp.set(false)
            }
        }
        state(ElevatorClampStates.UNCLAMPED){
            entry {
                clamp.set(true)
            }
        }
        default {
            entry {
                clamp.set(false)
            }
        }
    }

    on (Events.TELEOP_ENABLED) {
        elevatorDeployMachine.setState(ElevatorDeployStates.DEPLOYED)

        while (elevatorDeployMachine.getState() != ElevatorDeployStates.DEPLOYED) {
            Thread.sleep(1)
        }

        elevatorShifterMachine.setState(ElevatorShifterStates.RUN)
        elevatorClampMachine.setState(ElevatorClampStates.UNCLAMPED)
        elevatorKickerMachine.setState(ElevatorKickerStates.STOW)
        elevatorMachine.setState(ElevatorStates.OPEN_LOOP_CONTROL)

        println(elevatorMachine.getState())

        master.setSelectedSensorPosition(0, 0, 0)

    }

    on (RobotEvents.HAVE_CUBE) {
        elevatorClampMachine.setState(ElevatorClampStates.CLAMPED)
        elevatorMachine.setState(ElevatorStates.GO_TO_DRIVE)
    }

    test("Kicker test") {
        //test kicker
        elevatorKickerMachine.setState(ElevatorKickerStates.KICK)
        Thread.sleep(1000)
        elevatorKickerMachine.setState(ElevatorKickerStates.STOW)
        Thread.sleep(1000)
        elevatorKickerMachine.setState("")
        Thread.sleep(1000)

        true
    }
    test("Clamp test") {
        //test clamp
        elevatorClampMachine.setState(ElevatorClampStates.CLAMPED)
        Thread.sleep(1000)
        elevatorClampMachine.setState(ElevatorClampStates.UNCLAMPED)
        Thread.sleep(1000)
        elevatorClampMachine.setState("")
        Thread.sleep(1000)

        true
    }
    test("Ratchet test") {
        //test ratchet
        elevatorRatchetMachine.setState(ElevatorRatchetStates.LOCKED)
        Thread.sleep(1000)
        elevatorRatchetMachine.setState(ElevatorRatchetStates.UNLOCKED)
        Thread.sleep(1000)
        elevatorRatchetMachine.setState("")
        Thread.sleep(1000)

        true
    }
    test("Shifter test") {
        //test shifter
        elevatorShifterMachine.setState(ElevatorShifterStates.RUN)
        Thread.sleep(1000)
        elevatorShifterMachine.setState(ElevatorShifterStates.CLIMB)
        Thread.sleep(1000)
        elevatorShifterMachine.setState(ElevatorShifterStates.HOLD_CARRIAGE)
        Thread.sleep(1000)
        elevatorShifterMachine.setState("")
        Thread.sleep(1000)

        true
    }
    test("Elevator test") {
        //test elevator
        elevatorMachine.setState(ElevatorStates.HOMING)
        Thread.sleep(2000)
        elevatorMachine.setState(ElevatorStates.POS_MAX)

        var masterPower by Publisher(0.0)
        var slave1Power by Publisher(0.0)
        var slave2Power by Publisher(0.0)
        var slave3Power by Publisher(0.0)

        masterPower = PDP.getCurrent(4)
        slave1Power = PDP.getCurrent(5)
        slave2Power = PDP.getCurrent(6)
        slave3Power = PDP.getCurrent(7)

        Thread.sleep(2000)
        elevatorMachine.setState(ElevatorStates.POS_SCALE_HIGH)
        Thread.sleep(2000)
        elevatorMachine.setState(ElevatorStates.POS_SCALE)
        Thread.sleep(2000)
        elevatorMachine.setState(ElevatorStates.POS_SCALE_LOW)
        Thread.sleep(2000)
        elevatorMachine.setState(ElevatorStates.POS_COLLECTION)
        Thread.sleep(2000)
        elevatorMachine.setState("")
        Thread.sleep(1000)

        //results of the test
        val tolerance = 5.0
        (masterPower + slave1Power + slave2Power + slave3Power + tolerance) / 4 >= masterPower
    }
}
