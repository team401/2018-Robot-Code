package org.team401.robot2018.vision

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

data class VisionData(val isCubePresent: Boolean,
                      val cubeX: Double = 0.0,
                      val cubeY: Double = 0.0)