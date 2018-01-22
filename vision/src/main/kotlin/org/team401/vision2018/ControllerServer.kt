package org.team401.vision2018

import org.team401.snakeeyes.LockingDelegate
import org.team401.snakeeyes.service.Server
import org.team401.snakeeyes.view.View
import org.zeromq.ZMQ
import java.io.File
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.nio.file.Files
import java.nio.file.Paths
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

class ControllerServer(val view: View, val width: Int, val height: Int, val basePath: String = ".", val framerate: Double = 30.0, override val port: Int = 5801): Server {
    val context = ZMQ.context(1)
    lateinit var socket: ZMQ.Socket
    private var thread: Thread? = null

    private var recorder: VideoRecorder? = null

    private companion object {
        const val SETUP_WORD = "SETUP"
        const val START_WORD = "START"
        const val STOP_WORD = "STOP"

        const val SUCCESS_WORD = "SUCCESS"
        const val INVALID_WORD = "INVALID"
        const val STARTED_WORD = "STARTED"
        const val STOPPED_WORD = "STOPPED"
        const val RUNNING_WORD = "RUNNING"
    }

    private inner class SetupData(val actualTime: Long,
                                  val matchNo: String,
                                  val receivedTime: Long) {
        fun computeCurrentTime(time: Long) = actualTime + (time - receivedTime)
        fun getDate(time: Long) = Date(computeCurrentTime(time))
        private fun formatDate(date: Date) = date.toString().replace(' ', '_').replace(':', '-')
        fun createFilename() = "$matchNo-${formatDate(getDate(System.currentTimeMillis()))}.avi"
    }

    private var setupData: SetupData? = null
    private var fileNameOut: String? = null

    var recording by LockingDelegate(false); private set

    private fun moveFile() {
        val f = File("currentRecording.avi")
        println(f.toURI().toString())
        if (f.exists()) {
            val tmp = Files.move(Paths.get(f.toURI()), Paths.get(basePath).resolve(fileNameOut))

            if (tmp != null) {
                println("Controller: Copied file successfully")
            } else {
                println("Controller: Failed to copy file")
            }
        }
    }

    private fun deleteFile() {
        val f = File("currentRecording.avi")
        if (f.exists()) {
            f.delete()
        }
    }

    private fun task() {
        socket = context.socket(ZMQ.REP)
        socket.bind("tcp://*:$port")
        socket.receiveTimeOut = 20

        while (!Thread.interrupted()) {
            //"SETUP,time,matchNo"
            //"START"
            //"STOP"
            val recv: String? = socket.recvStr()
            if (recv != null && recv.isNotEmpty()) {
                if (recv.startsWith(SETUP_WORD)) {
                    val split = recv.split(",")
                    try {
                        val started = System.currentTimeMillis()
                        val epoch = split[1].toLong()
                        val matchNo = split[2]

                        setupData = SetupData(epoch, matchNo, started)

                        socket.send(SUCCESS_WORD)
                        println("Controller: Got valid setup data")
                    } catch (e: Exception) {
                        socket.send(INVALID_WORD)
                        println("Controller: Invalid setup data '$recv'")
                    }
                } else if (recv.startsWith(START_WORD)) {
                    if (!recording) {
                        if (setupData != null) {
                            val filename = setupData!!.createFilename()
                            deleteFile()
                            recorder = VideoRecorder(view, "currentRecording.avi", width, height, framerate)
                            recorder?.start()
                            socket.send(STARTED_WORD)
                            recording = true
                            fileNameOut = filename
                            println("Controller: Started recording '$filename'")
                        } else {
                            socket.send(INVALID_WORD)
                            println("Controller: Could not start a recording since no setup data was present")
                        }
                    } else {
                        socket.send(RUNNING_WORD)
                        println("Controller: Didn't start a recording because one was already running")
                    }
                } else if (recv.startsWith(STOP_WORD)) {
                    recorder?.stop()
                    socket.send(STOPPED_WORD)
                    recording = false
                    setupData = null

                    moveFile()
                    println("Controller: Stopped recording")
                } else {
                    System.err.println("Controller: Invalid command '$recv'")
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
