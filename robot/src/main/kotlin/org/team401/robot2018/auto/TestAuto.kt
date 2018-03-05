package org.team401.robot2018.auto.steps

import org.team401.robot2018.auto.*
import org.team401.robot2018.etc.StepAdder

object TestAuto : RobotAuto(){

    override fun assembleAuto(add: StepAdder) {

        add(DelayStep(1000))
        Routines.setup()

        add(DelayStep(500))

        add(Commands.ElevatorToDrive)
        add(DelayStep(500))

        Routines.drive(RobotPosition.DS_LEFT, FieldElements.switch(switch), SubSequence(Commands.ElevatorToSwitch))
        Routines.score()
    }


}