package org.team401.vision2018

import org.opencv.core.Point
import org.opencv.core.Rect

fun Rect.centerpoint() = Point(x + (width / 2.0), y + (height / 2.0))