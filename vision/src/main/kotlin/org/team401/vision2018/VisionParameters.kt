package org.team401.vision2018

data class VisionParameters(val procRegionStart: List<Int>,
                            val procRegionEnd: List<Int>,
                            val blurRadius: Double,
                            val hThresh: List<Double>,
                            val sThresh: List<Double>,
                            val vThresh: List<Double>,
                            val externalContoursOnly: Boolean,
                            val filtering: FilteringParameters,
                            val topCamera: CameraSettings,
                            val frontCamera: CameraSettings) {

    data class FilteringParameters(val minArea: Double,
                                   val minPerimeter: Double,
                                   val width: List<Double>,
                                   val height: List<Double>,
                                   val solidity: List<Double>,
                                   val vertices: List<Double>,
                                   val ratio: List<Double>)

    data class CameraSettings(val brightness: Int)
}