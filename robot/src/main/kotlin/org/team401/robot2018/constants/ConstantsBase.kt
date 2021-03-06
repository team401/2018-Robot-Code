package org.team401.robot2018.constants

import org.opencv.core.Rect
import org.opencv.core.Scalar
import  org.team401.robot2018.auto.motion.DriveGains
import org.team401.robot2018.auto.motion.PDVA
import org.team401.robot2018.etc.RobotMath

/*
 * 2018-Robot-Code - Created on 3/7/18
 * Author: Cameron Earle
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at project root
 */

/**
 * @author Cameron Earle
 * @version 3/7/18
 */
abstract class
ConstantsBase {
    interface PIDF {
        val P: Double
        val I: Double
        val D: Double
        val F: Double
    }
    
    class SetupConfig {
        class MJPEGParametersConfig {
            val ADDRESS = "10.4.1.3"
            val PORT = "1180"
            val FULL_ADDRESS = "mjpeg:https://${ADDRESS}:${PORT}/?action=stream"
        }
        val MJPEGParameters = MJPEGParametersConfig()
    }
    val Setup = SetupConfig()
    
    class MotorControllersConfig {
        val DRIVE_LEFT_REAR_CAN = 0
        val DRIVE_LEFT_MIDR_CAN = 1
        val DRIVE_LEFT_MIDF_CAN = 2
        val DRIVE_LEFT_FRONT_CAN = 3
        val DRIVE_RIGHT_FRONT_CAN = 11
        val DRIVE_RIGHT_MIDF_CAN = 12
        val DRIVE_RIGHT_MIDR_CAN = 13
        val DRIVE_RIGHT_REAR_CAN = 14

        val ELEVATOR_MASTER_CAN = 7
        val ELEVATOR_SLAVE_1_CAN = 10
        val ELEVATOR_SLAVE_2_CAN = 9
        val ELEVATOR_SLAVE_3_CAN = 8

        val INTAKE_LEFT_CAN = 5
        val INTAKE_RIGHT_CAN = 6
        val INTAKE_FOLDING_CAN = 4
    }
    val MotorControllers = MotorControllersConfig()

    class DIOConfig {
        val CUBE_BEAM_BREAK = 0
        val CUBE_LEFT_SENSOR = 1
        val CUBE_RIGHT_SENSOR = 2

        val CUBE_TRIGGERED = false
        val BEAM_BREAK_TRIGGERED = false
    }
    val DIO = DIOConfig()
    
    class PDPChannelsConfig {
        val DRIVE_LEFT_REAR_PDP = 15
        val DRIVE_LEFT_MIDR_PDP = 14
        val DRIVE_LEFT_MIDF_PDP = 13
        val DRIVE_LEFT_FRONT_PDP = 12
        val DRIVE_RIGHT_FRONT_PDP = 3
        val DRIVE_RIGHT_MIDF_PDP = 2
        val DRIVE_RIGHT_MIDR_PDP = 1
        val DRIVE_RIGHT_REAR_PDP = 0

        val ELEVATOR_MASTER_PDP = 7
        val ELEVATOR_SLAVE_1_PDP = 6
        val ELEVATOR_SLAVE_2_PDP = 5
        val ELEVATOR_SLAVE_3_PDP = 4

        val INTAKE_LEFT_PDP = 5
        val INTAKE_RIGHT_PDP = 6
        val INTAKE_FOLDING_PDP = 4
    }
    val PDPChannels = PDPChannelsConfig()
    
    class PneumaticsConfig {
        val SHIFTER_SOLENOID = 2

        val ELEVATOR_SHIFTER_SOLENOID = 3
        val ELEVATOR_DEPLOY_SOLENOID = 4
        val ELEVATOR_KICKER_SOLENOID = 6
        val ELEVATOR_CLAMP_SOLENOID = 5

        val RUNGS_DEPLOY_SOLENOID = 7 //OLD
        val ELEVATOR_RATCHET_SOLENOID = 7
    }
    val Pneumatics = PneumaticsConfig()
    
    abstract class DrivetrainParametersConfig {
        abstract val DELTA: Double
        abstract val SPEED_THRESHOLD: Double //f/s
        abstract val SPEED_SPLIT: Double //f/s
        //above should be some value between the low gear speed and the high gear speed

        abstract val WHEEL_DIAMETER: Double //in

        val ENCODER_TICKS = 4096.0

        val MIN_VELOCITY = -500.0 //RPM, negative
        val MAX_VELOCITY = 500.0 //RPM, positive

        val WHEEL_RADIUS = 2.0 //in
        val WHEELBASE = 0.0 //in

        val INVERT_LEFT = true
        val INVERT_RIGHT = false
        val INVERT_SHIFTER = true

        val CURRENT_LIMIT_CONTINUOUS = 30 //A
        val CURRENT_LIMIT_PEAK = 40 //A
        val CURRENT_LIMIT_TIMEOUT = 100 //ms

        val DOWNSHIFT_CURRENT = 30 //A

        val CLOSED_LOOP_RAMP = 0.0
        val OPEN_LOOP_RAMP = .25

        abstract val LEFT_PDVA: PDVA 
        abstract val RIGHT_PDVA: PDVA
        abstract val DRIVE_GAINS: DriveGains
        val DRIVE_MAGNITUDE = 0.8
        val HEADING_MAGNITUDE = 0.2

        abstract val HEADING_GAIN: Double
        abstract val HEADING_D: Double

        abstract val TIP_CORRECTION_SCALAR: Int //fixme (testme)

        abstract val PITCH_CORRECTION_MIN: Int //fixme (testme)
        abstract val ROLL_CORRECTION_MIN: Int //fixme (testme)
    }
    abstract val DrivetrainParameters: DrivetrainParametersConfig
    
    abstract class ElevatorParametersConfig {
        val DEPLOY_TIMER = 2700L //ms
        
        val MANUAL_RATE = 48 * .02 //inches per second (converted to inches per 20 ms)
        val CLIMB_MANUAL_RATE = 16 * .02

        val HOMING_RATE = -0.25
        
        val CURRENT_LIMIT_CONTINUOUS = 30 //A

        val CLIMB_OVERDRIVE_CURRENT = 10 //A
        val CLIMB_OVERDRIVE_COUNT = 10
        val CLIMB_OVERDRIVE_OFFSET = -100 //ticks to modify
        val CLIMB_OVERDRIVE_COOLDOWN = 25
        val CLIMB_MAX_POS = 47250.0
        
        val MIN_VELOCITY = -400.0
        val MAX_VELOCITY = 400.0
        
        val MAX_POS = 60000.0 //ticks

        val ZERO_POS = 0.0 //ticks
        val COLLECTION_POS = ZERO_POS + 500.0
        val CUBE_POS = RobotMath.Elevator.inchesToTicks(6.0) //ticks
        val SWITCH_POS = ZERO_POS + RobotMath.Elevator.inchesToTicks(32.0) //ticks
        val SCALE_POS = ZERO_POS + 48800 //ticks
        val SCALE_POS_HIGH = ZERO_POS + 60000.0 //ticks // MAX ELEVATOR POS
        val SCALE_POS_LOW = ZERO_POS + 37000.0 //ticks
        val CLIMB_PREP_POS = 40000.0
        val CLIMB_BOTTOM_POS = RobotMath.Elevator.inchesToTicks(36.0)
        val CLIMB_VERY_BOTTOM_POS = RobotMath.Elevator.inchesToTicks(9.0) //Accounting for platform height and 2 inch extra clearance

        val UNKNOWN_SCALE_POS = 25000.0

        val RATCHET_SERVO_PORT = 0
        abstract val RATCHET_UNLOCKED_SERVO_POS: Double
        abstract val RATCHET_LOCKED_SERVO_POS: Double

        val PITCH_DIAMETER = 1.805 //in

        val HOMING_COUNT = 10
        
        abstract val PIDF: PIDF

        class KickerMachineConfig {
            val EXTENDED = true
            val RETRACTED = false
        }
        val KickerMachine = KickerMachineConfig()
        
        class ClampMachineConfig {
            val DEPLOYED = true
            val RETRACTED = false
        }
        val ClampMachine = ClampMachineConfig()
        
        class RatchetMachineConfig {
            val LOCKED = true
            val UNLOCKED = false
        }
        val RatchetMachine = RatchetMachineConfig()
        
        class ShifterMachineConfig {
            val HIGH = true
            val LOW = false
            val HOLD = false
        }
        val ShifterMachine = ShifterMachineConfig()
        
        class DeployMachineConfig {
            val LOCKED = false
            val UNLOCKED = true
        }
        val DeployMachine = DeployMachineConfig()
    }
    abstract val ElevatorParameters: ElevatorParametersConfig
    
    abstract class IntakeParametersConfig {
        val INTAKE_RATE_LEFT = .7
        val INTAKE_RATE_RIGHT = .8
        val RETAIN_RATE = .6
        val REVERSE_RATE = -0.7
        val HOMING_RATE = -0.5

        val FOLDING_MIN_VELOCITY = -400.0 //RPM, negative
        val FOLDING_MAX_VELOCITY = 400.0 //RPM, positive

        val STOWED_POS = 0.0 + 170.0
        val INTAKE_POS = 2010.0 - 50.0
        val GRAB_POS = 1210.0 + 150.0
        val PAST_ELEVATOR_RAIL_POS = 1000.0

        val HOMING_COUNT = 10
        val BEAM_BREAK_COUNT = 5
        val HAVE_CUBE_COUNT = 15

        abstract val HAVE_CUBE_CURRENT_LEFT_HOLD: Double
        abstract val HAVE_CUBE_CURRENT_RIGHT_HOLD: Double
        abstract val HAVE_CUBE_CURRENT_LEFT_INTAKE: Double
        abstract val HAVE_CUBE_CURRENT_RIGHT_INTAKE: Double
        abstract val HAVE_CUBE_CURRENT_CLAMP: Double

        val INVERT_LEFT = true
        val INVERT_RIGHT = false

        val INTAKE_VOLTAGE = 12.0

        val FOLDING_PEAK_LIMIT = 30
        val FOLDING_CONTINUOUS_LIMIT = 10
        val FOLDING_PEAK_LIMIT_DUR = 100

        val FOLDING_PEAK_OUTPUT_FORWARD = 0.5
        val FOLDING_PEAK_OUTPUT_REVERSE = -0.5

        val LEFT_PEAK_LIMIT = 20
        val RIGHT_PEAK_LIMIT = 20

        val LEFT_CONTINUOUS_LIMIT = 15
        val RIGHT_CONTINUOUS_LIMIT = 15

        val LEFT_PEAK_LIMIT_DUR = 50
        val RIGHT_PEAK_LIMIT_DUR = 50

        val INRUSH_COUNT = 30
        val CUBE_HELD_TIME = 350L
        val HAVE_CUBE_CLAMP_DELAY = 70L

        abstract val PIDF: PIDF
    }
    abstract val IntakeParameters: IntakeParametersConfig

    class RungsParametersConfig {
        val DEPLOY_TIMER = 5000L //ms
    }
    val RungsParameters = RungsParametersConfig()

    class ReportingParametersConfig {
        val REPORTING_RATE = 100L //ms
    }
    val ReportingParameters = ReportingParametersConfig()

    class VisionParametersConfig {
        val CUBE_COLOR_MIN = Scalar(0.0, 0.0, 0.0)
        val CUBE_COLOR_MAX = Scalar(0.0, 0.0, 0.0)

        val CLOSE_ARMS_RECT = Rect()
        val HAVE_CUBE_RECT = Rect()
    }
    val VisionParameters = VisionParametersConfig()
}