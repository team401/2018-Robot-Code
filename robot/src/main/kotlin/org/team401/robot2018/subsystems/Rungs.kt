package org.team401.robot2018.subsystems

import edu.wpi.first.wpilibj.Solenoid
import org.snakeskin.dsl.*
import org.team401.robot2018.etc.Constants

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

val RUNGS_MACHINE = "rungs"
object RungsStates {
    const val STOWED = "stowed"
    const val DEPLOY = "deploy"
    const val DEPLOYED = "deployed"
}

object Rungs {
    lateinit var deployer: Solenoid
}

val RungsSubsystem: Subsystem = buildSubsystem {
    val deployer = Solenoid(Constants.Pneumatics.RUNGS_DEPLOY_SOLENOID)

    setup {
        Rungs.deployer = deployer
    }

    val rungsMachine = stateMachine(RUNGS_MACHINE) {
        //Constants for setting solenoid polarity
        val locked = false
        val unlocked = true

        state(RungsStates.STOWED) {
            entry {
                deployer.set(locked)
            }
        }

        state(RungsStates.DEPLOY) {
            timeout(Constants.RungsParameters.DEPLOY_TIMER, RungsStates.DEPLOYED)

            entry {
                deployer.set(unlocked)
            }
        }

        state(RungsStates.DEPLOYED) {
            entry {
                deployer.set(locked)
            }
        }

        default {
            entry {
                deployer.set(false)
            }
        }
    }

    test("Rungs") {
        rungsMachine.setState(RungsStates.STOWED)
        Thread.sleep(1000)

        rungsMachine.setState(RungsStates.DEPLOY)
        Thread.sleep(1000)

        rungsMachine.setState(RungsStates.DEPLOYED)
        Thread.sleep(1000)
        true
    }
}