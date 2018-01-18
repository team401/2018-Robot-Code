package org.team401.vision2018

import org.team401.snakeeyes.LockingDelegate
import org.team401.snakeeyes.service.Server
import org.team401.snakeeyes.view.View
import org.zeromq.ZMQ
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture

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

class ControllerServer(val view: View, val width: Int, val height: Int, val framerate: Double = 30.0, override val port: Int = 5801): Server {
    val context = ZMQ.context(1)
    lateinit var socket: ZMQ.Socket
    private var thread: Thread? = null

    private var recorder: VideoRecorder? = null

    private companion object {
        const val START_WORD = "START"
        const val STOP_WORD = "STOP"

        const val INVALID_WORD = "INVALID"
        const val STARTED_WORD = "STARTED"
        const val STOPPED_WORD = "STOPPED"
    }

    private fun createFilename(epoch: Long, matchNo: Int) = "$matchNo-${Date(epoch)}.avi"

    var recording by LockingDelegate(false); private set

    private fun task() {
        socket = context.socket(ZMQ.REP)
        socket.bind("tcp://*:$port")
        socket.receiveTimeOut = 20

        while (!Thread.interrupted()) {
            //"START,TIME,MATCH_NO"
            //"STOP"
            val recv: String? = socket.recvStr()
            if (recv != null && recv.isNotEmpty()) {
                if (recv.startsWith(START_WORD)) {
                    val split = recv.split(",")

                    if (split.size >= 3) {
                        try {
                            val epoch = split[1].toLong()
                            val matchNo = split[2].toInt()

                            val filename = createFilename(epoch, matchNo)

                            recorder = VideoRecorder(view, filename, width, height, framerate)
                            recorder?.start()
                            socket.send(STARTED_WORD)
                            recording = true
                            println("Started recording '$filename'")
                        } catch (e: Exception) {
                            System.err.println("Controller: Malformed START arguments [$recv]")
                            e.printStackTrace()
                            socket.send(INVALID_WORD)
                        }
                    } else {
                        System.err.println("Controller: Malformed START command [$recv]")
                        socket.send(INVALID_WORD)
                    }
                } else if (recv.startsWith(STOP_WORD)) {
                    recorder?.stop()
                    socket.send(STOPPED_WORD)
                    recording = false
                    println("Stopped recording")
                } else {
                    System.err.println("Controller: Invalid command [$recv]")
                    socket.send(INVALID_WORD)
                }
            }
        }
    }

    override fun start() {
        thread = Thread(::task)
        thread?.start()
    }

    override fun stop() {
        thread?.interrupt()
        thread?.join()
    }

}
