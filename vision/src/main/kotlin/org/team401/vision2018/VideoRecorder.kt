package org.team401.vision2018

import org.opencv.core.CvType
import org.opencv.core.Size
import org.opencv.videoio.VideoWriter
import org.team401.snakeeyes.service.Service
import org.team401.snakeeyes.view.View
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

/*
 * 2018-Robot-Code - Created on 1/17/18
 * Author: Cameron Earle
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at project root
 */

/**
 * @author Cameron Earle
 * @version 1/17/18
 */

class VideoRecorder(val view: View, val filename: String, val width: Int, val height: Int, val framerate: Double = 30.0,
                    val fourcc: Int = VideoWriter.fourcc('M', 'J', 'P', 'G')): Service {
    private val msPerFrame = (1000 / framerate).toLong()

    private val writer = VideoWriter()

    private val executor = Executors.newSingleThreadScheduledExecutor()
    private var future: ScheduledFuture<*>? = null

    private fun task() {
        if (writer.isOpened) {
            val rendered = view.render(width, height, CvType.CV_8UC3)
            writer.write(rendered)
            rendered.release()
        }
    }

    override fun start() {
        writer.open(filename, fourcc, framerate, Size(width.toDouble(), height.toDouble()))
        future = executor.scheduleAtFixedRate(::task, 0L, msPerFrame, TimeUnit.MILLISECONDS)
    }

    override fun stop() {
        future?.cancel(false)
        writer.release()
    }
}