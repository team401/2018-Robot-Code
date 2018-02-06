package org.team401.vision2018

import com.google.gson.Gson
import org.team401.snakeeyes.service.Server
import org.zeromq.ZMQ
import java.util.concurrent.ConcurrentLinkedQueue

/*
 * 2018-Robot-Code - Created on 2/5/18
 * Author: Cameron Earle
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at project root
 */

/**
 * @author Cameron Earle
 * @version 2/5/18
 */

class VisionDataServer(override val port: Int): Server {
    private val queue = ConcurrentLinkedQueue<VisionData>()
    private val context = ZMQ.context(1)
    private lateinit var socket: ZMQ.Socket
    private val thread = Thread(::run)
    private val gson = Gson()

    fun send(data: VisionData) = queue.add(data)

    private fun run() {
        socket = context.socket(ZMQ.PUB)
        socket.bind("tcp://*:$port")

        var latest: VisionData? = queue.poll() //Grab data for the first time
        while (!Thread.interrupted()) { //While this thread should run
            while (latest != null) { //While there is data
                socket.send(gson.toJson(latest)) //Send the data
                latest = queue.poll() //Grab new data
            }
            try {
                Thread.sleep(10L)
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
            }
        }
    }

    override fun start() {
        thread.start()
    }

    override fun stop() {
        thread.interrupt()
    }
}