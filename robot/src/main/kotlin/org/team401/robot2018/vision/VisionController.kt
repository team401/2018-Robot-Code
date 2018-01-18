package org.team401.robot2018.vision

import org.snakeskin.factory.ExecutorFactory
import org.zeromq.ZMQ

/*
 * 2018-Robot-Code - Created on 1/18/18
 * Author: Cameron Earle
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at project root
 */

/**
 * @author Cameron Earle
 * @version 1/18/18
 */
class VisionController(address: String, port: Int = 5801) {
    val executor = ExecutorFactory.getSingleExecutor("visionController")

    private val context = ZMQ.context(0)
    private lateinit var socket: ZMQ.Socket

    enum class Commands(val cmdStr: String) {
        START("START"),
        STOP("STOP")
    }

    init {
        executor.submit {
            socket = context.socket(ZMQ.REQ)
            socket.connect("tcp://$address:$port")
        }
    }

    fun sendCommand(command: Commands) {
        executor.submit {
            socket.send(command.cmdStr)
            socket.recv()
        }
    }

    fun startRecording() = sendCommand(Commands.START)
    fun stopRecording() = sendCommand(Commands.STOP)
}