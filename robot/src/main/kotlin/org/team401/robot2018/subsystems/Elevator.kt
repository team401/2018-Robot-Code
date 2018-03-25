package org.team401.robot2018.subsystems

import com.ctre.phoenix.ParamEnum
import com.ctre.phoenix.motorcontrol.*
import com.ctre.phoenix.motorcontrol.can.TalonSRX
import com.ctre.phoenix.motorcontrol.can.VictorSPX
import edu.wpi.first.wpilibj.Solenoid
import org.snakeskin.component.Gearbox
import org.snakeskin.dsl.Subsystem
import org.snakeskin.dsl.buildSubsystem
import org.snakeskin.event.Events
import org.snakeskin.logic.Direction
import org.snakeskin.logic.LockingDelegate
import org.team401.robot2018.Gamepad
import org.team401.robot2018.RightStick
import org.team401.robot2018.constants.Constants
import org.team401.robot2018.etc.*
import java.io.File
import java.io.PrintWriter
import java.util.*

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

const val ELEVATOR_DEPLOY_MACHINE = "elevator_deploy"
object ElevatorDeployStates {
    const val STOWED = "stowed"
    const val DEPLOY = "deploy"
    const val DEPLOYED = "deployed"
}

const val ELEVATOR_MACHINE = "elevator"
object ElevatorStates {
    const val OPEN_LOOP_CONTROL = "openloop"
    const val MANUAL_ADJUSTMENT = "closedloop"
    const val HOLD_POS_UNKNOWN = "pos_lock"
    const val SCALE_POS_UNKNOWN = "scaleStart"
    const val HOMING = "homing"

    const val GO_TO_DRIVE = "goToDrive"
    const val GO_TO_COLLECTION = "goToCollection"

    const val POS_COLLECTION = "ground"
    const val POS_DRIVE = "hold"
    const val POS_SWITCH = "switchSide"
    const val POS_SCALE_LOW = "scale_low"
    const val POS_SCALE = "scaleSide"
    const val POS_SCALE_HIGH = "scale_high"
    const val POS_MAX = "max"
    const val POS_VAULT_RUNNER = "vault-outOfWay"

    const val START_CLIMB = "startClimb"
    const val CLIMB_MANUAL = "climbAlign"
    const val CLIMB = "climb"
}

const val ELEVATOR_SHIFTER_MACHINE = "elevator_shifter"
object ElevatorShifterStates {
    const val HIGH = "high"
    const val LOW = "low"
}

const val ELEVATOR_RATCHET_MACHINE = "elevator_ratchet"
object ElevatorRatchetStates {
    const val LOCKED = "locked"
    const val UNLOCKED = "unlocked"
}

const val ELEVATOR_KICKER_MACHINE = "elevator_kicker"
object ElevatorKickerStates {
    const val KICK = "out"
    const val STOW = "in"
}

const val ELEVATOR_CLAMP_MACHINE = "elevator_clamp"
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
    var estop by LockingDelegate(false)

    fun atSwitch() = gearbox.master.getSelectedSensorPosition(0).toDouble().withinTolerance(Constants.ElevatorParameters.SWITCH_POS, 1000.0)
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
    val kicker = Solenoid(Constants.Pneumatics.ELEVATOR_KICKER_SOLENOID)
    val clamp = Solenoid(Constants.Pneumatics.ELEVATOR_CLAMP_SOLENOID)
    val ratchet = Solenoid(Constants.Pneumatics.ELEVATOR_RATCHET_SOLENOID)

    fun runMode() {
        master.configMotionCruiseVelocity(21680, 0)
        master.configMotionAcceleration(21680, 0)
    }

    fun climbMode() {
        master.configMotionCruiseVelocity(217*14, 0)
        master.configMotionAcceleration(21680, 0)
    }

    fun finalClimbMode() {
        master.configMotionCruiseVelocity(217 * 14, 0)
        master.configMotionAcceleration(21680, 0)
    }

    setup {
        Elevator.gearbox = gearbox
        Elevator.shifter = shifter
        Elevator.deployer = deployer
        Elevator.ratchet = ratchet
        Elevator.kicker = kicker
        Elevator.clamp = clamp
        
        gearbox.setSensor(FeedbackDevice.CTRE_MagEncoder_Absolute)

        runMode()
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
                deployer.set(false)
            }
        }

        state(ElevatorDeployStates.DEPLOY) {
            timeout(Constants.ElevatorParameters.DEPLOY_TIMER, ElevatorDeployStates.DEPLOYED)

            entry {
                deployer.set(true)
            }
        }

        state(ElevatorDeployStates.DEPLOYED) {
            entry {
                deployer.set(false)
            }
        }

        default {
            entry {
                deployer.set(false)
            }
        }
    }

    fun notDeployed() = elevatorDeployMachine.getState() != ElevatorDeployStates.DEPLOYED

    val elevatorShifterMachine = stateMachine(ELEVATOR_SHIFTER_MACHINE) {
        state(ElevatorShifterStates.HIGH) {
            entry {
                shifter.set(Constants.ElevatorParameters.ShifterMachine.HIGH)
            }
        }

        state(ElevatorShifterStates.LOW) {
            entry {
                shifter.set(Constants.ElevatorParameters.ShifterMachine.LOW)
            }
        }

        default {
            entry {
                shifter.set(false)
            }
        }
    }

    val elevatorClampMachine = stateMachine(ELEVATOR_CLAMP_MACHINE) {
        state(ElevatorClampStates.CLAMPED){
            entry {
                clamp.set(false)
            }
        }
        state(ElevatorClampStates.UNCLAMPED){
            entry {
                clamp.set(true)
            }
        }
    }


    val elevatorMachine = stateMachine(ELEVATOR_MACHINE) {
        fun mmSetpoint(setpoint: Number) {
            if (!Elevator.estop) {
                runMode()
                gearbox.set(ControlMode.MotionMagic, setpoint.toDouble())
            } else {
                gearbox.set(ControlMode.PercentOutput, 0.0)
            }
        }

        fun climbSetpoint(setpoint: Number) {
            if (!Elevator.estop) {
                climbMode()
                gearbox.set(ControlMode.MotionMagic, setpoint.toDouble())
            } else {
                gearbox.set(ControlMode.PercentOutput, 0.0)
            }
        }

        fun finalClimbSetpoint(setpoint: Number) {
            if (!Elevator.estop) {
                finalClimbMode()
                gearbox.set(ControlMode.MotionMagic, setpoint.toDouble())
            } else {
                gearbox.set(ControlMode.PercentOutput, 0.0)
            }
        }

        state(ElevatorStates.OPEN_LOOP_CONTROL) {
            action {
                gearbox.set(ControlMode.PercentOutput, 0.0)
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
            rejectIf { isInState(ElevatorStates.POS_VAULT_RUNNER) }

            action {
                if (Intake.stowed() || Intake.atGrab()) {
                    setState(ElevatorStates.POS_COLLECTION)
                }
            }
        }

        state(ElevatorStates.POS_COLLECTION) {
            

            entry {
                mmSetpoint(Constants.ElevatorParameters.COLLECTION_POS)
            }
        }

        state(ElevatorStates.POS_DRIVE) {
            

            entry {
                mmSetpoint(Constants.ElevatorParameters.CUBE_POS)
            }
        }

        state(ElevatorStates.POS_SWITCH) {
            

            entry {
                mmSetpoint(Constants.ElevatorParameters.SWITCH_POS)
            }
        }

        state(ElevatorStates.POS_SCALE_LOW) {
            

            entry {
                mmSetpoint(Constants.ElevatorParameters.SCALE_POS_LOW)
            }
        }

        state(ElevatorStates.POS_SCALE) {
            

            entry {
                mmSetpoint(Constants.ElevatorParameters.SCALE_POS)
            }
        }

        state (ElevatorStates.POS_SCALE_HIGH) {
            

            entry {
                mmSetpoint(Constants.ElevatorParameters.SCALE_POS_HIGH)
            }
        }

        state (ElevatorStates.POS_MAX) {
            

            entry {
                mmSetpoint(Constants.ElevatorParameters.MAX_POS)
            }
        }

        state(ElevatorStates.HOLD_POS_UNKNOWN) {
            entry {
                gearbox.setPosition(0)
                mmSetpoint(0.0)
            }
        }

        state(ElevatorStates.SCALE_POS_UNKNOWN) {
            entry {
                mmSetpoint(Constants.ElevatorParameters.UNKNOWN_SCALE_POS)
            }
        }

        state(ElevatorStates.POS_VAULT_RUNNER) {
            entry {
                mmSetpoint(Constants.ElevatorParameters.SWITCH_POS)
            }
        }

        state("tuning") {
            var zeroCounter = 0
            var positionDesired = 0
            var velocityDesired = 0
            var positionActual = 0
            var velocityActual = 0
            var actualPower = 0.0
            var time = 0L
            var startTime = 0L
            var oldFrameRate = 0
            var oldEncoderRate = 0
            lateinit var file: File
            lateinit var writer: PrintWriter

            entry {
                try {
                    file = File("/home/lvuser/elevatorTuning/plant-${Date(System.currentTimeMillis()).toString().replace(' ', '_').replace(':', '-')}.csv")
                    writer = file.printWriter()
                    writer.println("P,I,D,F,I Zone")
                    writer.println("${Elevator.gearbox.master.configGetParameter(ParamEnum.eProfileParamSlot_P, 0, 0)}," +
                            "${Elevator.gearbox.master.configGetParameter(ParamEnum.eProfileParamSlot_I, 0, 0)}," +
                            "${Elevator.gearbox.master.configGetParameter(ParamEnum.eProfileParamSlot_D, 0, 0)}," +
                            "${Elevator.gearbox.master.configGetParameter(ParamEnum.eProfileParamSlot_F, 0, 0)}," +
                            "${Elevator.gearbox.master.configGetParameter(ParamEnum.eProfileParamSlot_IZone, 0, 0)}")
                    writer.println("Time, Desired Position, Desired Velocity, Actual Position, Actual Velocity")
                }catch(e : Exception){
                    println(e.message)
                }
                oldFrameRate = master.getStatusFramePeriod(StatusFrame.Status_10_MotionMagic, 100)
                oldEncoderRate = master.getStatusFramePeriod(StatusFrameEnhanced.Status_1_General, 100)
                master.setStatusFramePeriod(StatusFrame.Status_10_MotionMagic, 10, 0)
                master.setStatusFramePeriod(StatusFrame.Status_1_General, 10, 0)
                zeroCounter = 0
                positionDesired = 0
                velocityDesired = 0
                positionActual = 0
                velocityActual = 0
                actualPower = 0.0
                time = 0
                mmSetpoint(Constants.ElevatorParameters.SCALE_POS)
                startTime = System.currentTimeMillis()
            }

            action(10) {
                positionDesired = master.activeTrajectoryPosition
                velocityDesired = master.activeTrajectoryVelocity
                positionActual = master.getSelectedSensorPosition(0)
                velocityActual = master.getSelectedSensorVelocity(0)
                time = System.currentTimeMillis() - startTime

                actualPower = master.motorOutputPercent

                if (velocityDesired == 0) {
                    zeroCounter++
                } else {
                    zeroCounter = 0
                }

                if (zeroCounter > 10) {
                    setState(ElevatorStates.POS_SCALE)
                }

                writer.println("$time,$positionDesired,$velocityDesired,$positionActual,$velocityActual")
            }
            exit{
                writer.flush()
                writer.close()

                master.setStatusFramePeriod(StatusFrame.Status_10_MotionMagic, oldFrameRate, 0)
                master.setStatusFramePeriod(StatusFrame.Status_1_General, oldEncoderRate,0)
            }
        }

        state(ElevatorStates.HOMING) {
            rejectIf { elevatorDeployMachine.getState() != ElevatorDeployStates.DEPLOYED }

            var homingCounter = 0
            var velocity: Int

            entry {
                Elevator.homed = false
                homingCounter = 0
                elevatorShifterMachine.setState(ElevatorShifterStates.HIGH)
                Thread.sleep(100)
            }

            action {
                velocity = master.getSelectedSensorVelocity(0)
                gearbox.set(ControlMode.PercentOutput, Constants.ElevatorParameters.HOMING_RATE) //Run down at the homing rate

                if (velocity == 0) {
                    homingCounter++
                } else {
                    homingCounter = 0
                }

                if (homingCounter > Constants.ElevatorParameters.HOMING_COUNT) {
                    gearbox.setPosition(0)
                    Elevator.homed = true
                    setState(ElevatorStates.POS_DRIVE)
                }
            }
        }

        state(ElevatorStates.MANUAL_ADJUSTMENT) {
            var position = 0

            entry {
                position = gearbox.getPosition()
            }

            action {
                if (Gamepad.readButton { LEFT_STICK }) {
                    position += RobotMath.Elevator.inchesToTicks((Gamepad.readAxis { LEFT_Y } * Constants.ElevatorParameters.MANUAL_RATE)).toInt()
                }
                if (position > Constants.ElevatorParameters.MAX_POS) position = Constants.ElevatorParameters.MAX_POS.toInt()
                if (position < Constants.ElevatorParameters.ZERO_POS) position = Constants.ElevatorParameters.ZERO_POS.toInt()
                mmSetpoint(position)
            }
        }

        state(ElevatorStates.START_CLIMB) {
            timeout(5000L, ElevatorStates.CLIMB_MANUAL)

            entry {
                mmSetpoint(Constants.ElevatorParameters.CLIMB_PREP_POS)
            }

            action {
                if (master.getSelectedSensorPosition(0).toDouble().withinTolerance(Constants.ElevatorParameters.CLIMB_PREP_POS, 1000.0)) {
                    setState(ElevatorStates.CLIMB_MANUAL)
                }
            }
        }

        state(ElevatorStates.CLIMB_MANUAL) {
            var position = 0
            var scalar = 0.0

            entry {
                scalar = 0.0
                position = gearbox.getPosition()
            }

            action {
                scalar = when(RightStick.readHat { STICK_HAT }) {
                    Direction.NORTH -> 1.0
                    Direction.SOUTH -> -1.0
                    else -> 0.0
                }
                position += RobotMath.Elevator.inchesToTicks(scalar * Constants.ElevatorParameters.CLIMB_MANUAL_RATE).toInt()
                if (position > Constants.ElevatorParameters.MAX_POS) position = Constants.ElevatorParameters.MAX_POS.toInt()
                if (position < Constants.ElevatorParameters.ZERO_POS) position = Constants.ElevatorParameters.ZERO_POS.toInt()
                climbSetpoint(position)
            }
        }

        state(ElevatorStates.CLIMB) {
            rejectIf { !isInState(ElevatorStates.CLIMB_MANUAL) }
            entry {
                finalClimbSetpoint(Constants.ElevatorParameters.CLIMB_BOTTOM_POS)
            }
        }

        default {
            action {
                gearbox.set(ControlMode.PercentOutput, 0.0)
            }
        }
    }

    val elevatorRatchetMachine = stateMachine(ELEVATOR_RATCHET_MACHINE) {
        state(ElevatorRatchetStates.LOCKED) {
            entry {
                ratchet.set(Constants.ElevatorParameters.RatchetMachine.LOCKED)
                //ratchet.angle = Constants.ElevatorParameters.RATCHET_LOCKED_SERVO_POS
            }
        }

        state(ElevatorRatchetStates.UNLOCKED) {
            entry {
                ratchet.set(Constants.ElevatorParameters.RatchetMachine.UNLOCKED)
                //ratchet.angle = Constants.ElevatorParameters.RATCHET_UNLOCKED_SERVO_POS
            }
        }

        default {
            entry {
                ratchet.set(false)
                //ratchet.set(0.0)
            }
        }
    }

    val elevatorKickerMachine = stateMachine(ELEVATOR_KICKER_MACHINE) {

        state(ElevatorKickerStates.KICK) {
            entry {
                kicker.set(true)
            }
        }

        state(ElevatorKickerStates.STOW) {
            entry {
                kicker.set(false)
            }
        }

        default {
            entry {
                kicker.set(false)
            }
        }
    }

    on (Events.TELEOP_ENABLED) {
        /*
        if (notDeployed()) { //If we aren't deployed
            elevatorDeployMachine.setState(ElevatorDeployStates.DEPLOY) //Deploy
            while (notDeployed()) { //Wait for deploy to finish
                Thread.sleep(10)
            }
        }
        */
        //Assume we are already deployed
        elevatorDeployMachine.setState(ElevatorDeployStates.DEPLOYED)

        if (!Elevator.homed) { //If we aren't homed
            Thread.sleep(1000) //Wait a second for us to back up
            elevatorShifterMachine.setState(ElevatorShifterStates.HIGH) //High gear
            elevatorMachine.setState(ElevatorStates.HOMING) //Home
        } else {
            elevatorShifterMachine.setState(ElevatorShifterStates.HIGH) //High gear
            elevatorMachine.setState(ElevatorStates.POS_DRIVE) //Go to driving position
        }

        //Always put all machines in a known state on enable
        elevatorClampMachine.setState(ElevatorClampStates.UNCLAMPED)
        elevatorKickerMachine.setState(ElevatorKickerStates.STOW)
        elevatorRatchetMachine.setState(ElevatorRatchetStates.UNLOCKED)
    }

    on (RobotEvents.HAVE_CUBE) {
        if (elevatorMachine.getState() != ElevatorStates.POS_VAULT_RUNNER) {
            elevatorClampMachine.setState(ElevatorClampStates.CLAMPED)
            elevatorMachine.setState(ElevatorStates.GO_TO_DRIVE)
        }
    }
    on (RobotEvents.EJECT_CUBE){
        if (elevatorMachine.getState() != ElevatorStates.POS_VAULT_RUNNER) {
            elevatorMachine.setState(ElevatorStates.POS_COLLECTION)
        }
    }
}
