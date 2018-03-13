package org.team401.robot2018.vision

import org.opencv.core.Mat
import org.opencv.core.MatOfByte
import org.opencv.core.MatOfInt
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.videoio.VideoCapture
import java.io.ByteArrayOutputStream
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketTimeoutException
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

/*
 * 2018-Robot-Code - Created on 3/9/18
 * Author: Cameron Earle
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at project root
 */

/**
 * @author Cameron Earle
 * @version 3/9/18
 */
class MjpegServer (val port: Int, val camera: Int= 0, val width: Int = 640, val height: Int = 480,
                  framerate: Int = 30, val quality: Int = 30, val timeout: Int = 2000) {

    private companion object {
        val TERM = "\r\n\r\n".toByteArray()
        val HEAD = (
                "HTTP/1.0 200 OK\r\n" +
                        "Server: SnakeEyes\r\n" +
                        "Connection: close\r\n" +
                        "Max-Age: 0\r\n" +
                        "Expires: 0\r\n" +
                        "Cache-Control: no-cache, private\r\n" +
                        "Pragma: no-cache\r\n" +
                        "Content-Type: multipart/x-mixed-replace; " +
                        "boundary=--BoundaryString\r\n\r\n"
                ).toByteArray()
    }

    private val msPerFrame = (1000/framerate).toLong()

    private lateinit var server: ServerSocket
    private var serverThread: Thread? = null
    private var future: ScheduledFuture<*>? = null
    private val clients = Vector<Socket>()
    private val executor = Executors.newSingleThreadScheduledExecutor()
    private lateinit var cap: VideoCapture

    private inner class ConnHandler: Runnable {
        override fun run() {
            while (!Thread.interrupted()) {
                try {
                    val client = server.accept()
                    client.soTimeout = timeout
                    client.getOutputStream().write(HEAD)
                    clients.add(client)
                } catch (e: SocketTimeoutException) {}
            }
        }
    }

    private inner class StreamHandler: Runnable {
        val baos = ByteArrayOutputStream()
        val mob = MatOfByte()
        val mat = Mat()

        val options = MatOfInt(Imgcodecs.CV_IMWRITE_JPEG_QUALITY, quality)
        val toRemove = arrayListOf<Socket>()

        override fun run() {
            if (clients.isNotEmpty()) {
                cap.read(mat)
                Imgcodecs.imencode(".jpg", mat, mob, options)
                mat.release()
                baos.write(mob.toArray())
                mob.release()
                clients.forEach {
                    try {
                        val os = it.getOutputStream()
                        os.write((
                                "--BoundaryString\r\n" +
                                        "Content-type: image/jpeg\r\n" +
                                        "Content-Length: " +
                                        baos.size() +
                                        "\r\n\r\n"
                                ).toByteArray())
                        baos.writeTo(os)
                        os.write(TERM)
                        os.flush()
                    } catch (e: Exception) { //Client disconnected
                        toRemove.add(it)
                    }
                }
                clients.removeAll(toRemove)
                toRemove.clear()
                baos.reset()
            }
        }
    }

    fun start() {
        cap = VideoCapture(camera)
        server = ServerSocket()
        server.setPerformancePreferences(0, 1, 2)
        server.soTimeout = 100
        server.bind(InetSocketAddress(port))
        serverThread = Thread(ConnHandler())
        serverThread?.start()
        future = executor.scheduleAtFixedRate(StreamHandler(), 0L, msPerFrame, TimeUnit.MILLISECONDS)
    }

    fun stop() {
        future?.cancel(false)
        serverThread?.interrupt()
        serverThread?.join()
        server.close()
        cap.release()
    }
}