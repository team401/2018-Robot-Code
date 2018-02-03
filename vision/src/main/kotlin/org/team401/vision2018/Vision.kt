package org.team401.vision2018

import com.google.gson.Gson
import org.opencv.core.Core
import org.team401.snakeeyes.Cameras
import org.team401.snakeeyes.camera.Camera
import org.team401.snakeeyes.service.MjpegServer
import org.team401.snakeeyes.view.CameraView
import org.team401.snakeeyes.view.GridView
import org.team401.snakeeyes.view.PipView
import java.io.File

/*
 * 2018-Robot-Code - Created on 1/5/18
 * Author: Cameron Earle
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at project root
 */

/**
 * @author Cameron Earle
 * @version 1/5/18
 */

fun main(args: Array<String>) {
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME)

    val top = Camera()
    val front = Camera()

    val topView = CameraView(top)
    val frontView = CameraView(front)

    top.open("/dev/v4l/by-path/platform-70090000.xusb-usb-0:1:1.0-video-index0")
    front.open("/dev/v4l/by-path/platform-70090000.xusb-usb-0:3.1:1.0-video-index0")

    Cameras.add(top)
    Cameras.add(front)
    Cameras.start()

    val view = PipView(topView, frontView, PipView.Position.BOTTOM_CENTER, .5)
    val server = MjpegServer(1180, view)
    server.start()

    val sideBySide = GridView(1, 2)
    sideBySide.putView(topView, 0, 0)
    sideBySide.putView(frontView, 0, 1)

    val controller = ControllerServer(sideBySide, 640 * sideBySide.cols, 480 * sideBySide.rows, "/media/2018REC")
    controller.start()

    val paramFile = File("parameters.json")
    val params = Gson().fromJson(paramFile.readText(), VisionParameters::class.java)

    val cubeFinder = CubeFinderPipeline(top, params)
    cubeFinder.start()
}