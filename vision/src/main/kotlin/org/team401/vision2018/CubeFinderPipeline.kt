package org.team401.vision2018

import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import org.team401.snakeeyes.MatProvider
import org.team401.snakeeyes.pipeline.Pipeline

class CubeFinderPipeline(provider: MatProvider, val p: VisionParameters): Pipeline(provider) {
    val procRangeRect = Rect(Point(p.procRegionStart[0].toDouble(), p.procRegionStart[1].toDouble()),
                             Point(p.procRegionEnd[0].toDouble(), p.procRegionEnd[1].toDouble()))

    val blurKernel = Size(2.0 * (p.blurRadius + 0.5).toInt() + 1, 2.0 * (p.blurRadius + 0.5).toInt() + 1)

    val threshLower = Scalar(p.hThresh[0], p.sThresh[0], p.vThresh[0])
    val threshUpper = Scalar(p.hThresh[1], p.sThresh[1], p.vThresh[1])

    val contourMode = if (p.externalContoursOnly) Imgproc.RETR_EXTERNAL else Imgproc.RETR_LIST


    val contours = arrayListOf<MatOfPoint>()
    override fun process(mat: Mat) {
        //Grab the region to process
        val procMat = mat.submat(procRangeRect)

        //Blur the frame to reduce noise
        Imgproc.blur(procMat, procMat, blurKernel)

        //Convert the image to HSV color space
        Imgproc.cvtColor(procMat, procMat, Imgproc.COLOR_BGR2HSV)

        //Threshold out unwanted colors
        Core.inRange(procMat, threshLower, threshUpper, procMat)

        //Find contours in the image
        val hierarchy = Mat()
        contours.clear()
        Imgproc.findContours(procMat, contours, hierarchy, contourMode, Imgproc.CHAIN_APPROX_SIMPLE)

        //Filter contours
        val filteredContours = filterContours(contours)

        //Check if cube is present
        val isCubePresent = filteredContours.isNotEmpty()

        //TODO publish data
    }

    private fun checkBounds(value: Number, bounds: List<Number>) = bounds.size >= 2 && (value.toDouble() < bounds[0].toDouble() || value.toDouble() > bounds[1].toDouble())

    private fun filterContours(contours: List<MatOfPoint>): List<MatOfPoint> {
        val filtered = arrayListOf<MatOfPoint>()
        val hull = MatOfInt()

        p.filtering.run {
            for (contour in contours) {
                val bb = Imgproc.boundingRect(contour)
                if (checkBounds(bb.width, width)) continue
                if (checkBounds(bb.height, height)) continue
                val area = Imgproc.contourArea(contour)
                if (area < minArea) continue
                if (Imgproc.arcLength(MatOfPoint2f(contour), true) < minPerimeter) continue
                Imgproc.convexHull(contour, hull)
                val mopHull = MatOfPoint()
                mopHull.create(hull.size().height.toInt(), 1, CvType.CV_32SC2)
                for (i in 0..hull.size().height.toInt()) {
                    val index = hull.get(i, 0)[0].toInt()
                    mopHull.put(i, 0, contour.get(index, 0)[0], contour.get(index, 0)[1])
                }
                val solid = 100 * area / Imgproc.contourArea(mopHull)
                if (checkBounds(solid, solidity)) continue
                if (checkBounds(contour.rows(), vertices)) continue
                val ratio = bb.width / bb.height.toDouble()
                if (checkBounds(ratio, p.filtering.ratio)) continue
                filtered.add(contour)
            }
        }

        return filtered
    }
}