package org.team401.robot2018.vision

import com.google.gson.Gson
import org.snakeskin.logic.LockingDelegate
import org.zeromq.ZMQ

/*
 * 2018-Robot-Code - Created on 2/6/18
 * Author: Cameron Earle
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at project root
 */

/**
 * @author Cameron Earle
 * @version 2/6/18
 */

class VisionDataClient(val address: String = "10.4.1.3", val port: Int) {
    private var latestData by LockingDelegate(VisionData(false, 0.0, 0.0))

    private val context = ZMQ.context(1)
    private lateinit var socket: ZMQ.Socket
    private val gson = Gson()
    private val thread = Thread(this::run)

    private fun run() {
        socket = context.socket(ZMQ.SUB)
        socket.subscribe("".toByteArray())
        socket.connect("tcp:$address:$port")
        socket.receiveTimeOut = 10 //ms
        var latest: String? = ""

        while (!Thread.interrupted()) {
            try {
                latest = socket.recvStr()
                if (latest != null && latest != "") {
                    latestData = gson.fromJson(latest, VisionData::class.java)
                }
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
            }
        }
    }

    fun start() {
        thread.start()
    }

    fun stop() {
        thread.interrupt()
    }

    fun read() = latestData
}