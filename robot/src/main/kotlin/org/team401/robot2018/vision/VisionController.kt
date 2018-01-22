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

    init {
        executor.submit {
            socket = context.socket(ZMQ.REQ)
            socket.connect("tcp://$address:$port")
        }
    }

    fun sendCommand(command: String) {
        executor.submit {
            socket.send(command)
            socket.recv()
        }
    }

    fun setup(time: Long, matchNo: String) = sendCommand("SETUP,$time,$matchNo")
    fun start() = sendCommand("START")
    fun stop() = sendCommand("STOP")
}