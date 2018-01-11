package org.team401.vision2018

import org.opencv.core.Core
import org.team401.snakeeyes.Cameras
import org.team401.snakeeyes.camera.Camera
import org.team401.snakeeyes.service.MjpegServer
import org.team401.snakeeyes.view.CameraView

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
    val cam = Camera()
    cam.open(0)

    Cameras.add(cam)
    Cameras.start()

    val view = CameraView(cam)
    val server = MjpegServer(8080, view)
    server.start()
}