package org.team401.vision2018

import org.opencv.core.Core
import org.team401.snakeeyes.Cameras
import org.team401.snakeeyes.camera.Camera
import org.team401.snakeeyes.service.MjpegServer
import org.team401.snakeeyes.view.CameraView
import org.team401.snakeeyes.view.PipView

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

    val topView = Camera()
    val frontView = Camera()

    topView.open("TOP_PATH")        //TODO add correct paths (/dev/v4l/by-id/...)
    frontView.open("FRONT_PATH")

    Cameras.add(topView)
    Cameras.add(frontView)
    Cameras.start()

    val view = PipView(CameraView(topView), CameraView(frontView), PipView.Position.TOP_CENTER)
    val server = MjpegServer(1180, view)
    server.start()
}