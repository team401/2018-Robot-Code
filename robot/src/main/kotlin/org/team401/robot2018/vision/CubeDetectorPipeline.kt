package org.team401.robot2018.vision

import edu.wpi.first.wpilibj.vision.VisionPipeline
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.imgproc.Imgproc
import org.snakeskin.logic.LockingDelegate
import org.team401.robot2018.constants.Constants

object CubeDetectorPipeline: VisionPipeline {
    var closeArms by LockingDelegate(false); private set
    var haveCube by LockingDelegate(false); private set

    private var closeArmsSubmat = Mat()
    private var haveCubeSubmat = Mat()
    private val closeArmsHierarchy = Mat()
    private val haveCubeHierarchy = Mat()
    private val closeArmsContours = arrayListOf<MatOfPoint>()
    private val haveCubeContorus = arrayListOf<MatOfPoint>()

    override fun process(image: Mat) {
        closeArmsSubmat = image.submat(Constants.VisionParameters.CLOSE_ARMS_RECT)
        haveCubeSubmat = image.submat(Constants.VisionParameters.HAVE_CUBE_RECT)

        Imgproc.cvtColor(closeArmsSubmat, closeArmsSubmat, Imgproc.COLOR_BGR2HSV_FULL)
        Core.inRange(closeArmsSubmat, Constants.VisionParameters.CUBE_COLOR_MIN, Constants.VisionParameters.CUBE_COLOR_MAX, closeArmsSubmat)
        Imgproc.cvtColor(haveCubeSubmat, haveCubeSubmat, Imgproc.COLOR_BGR2HSV_FULL)
        Core.inRange(haveCubeSubmat, Constants.VisionParameters.CUBE_COLOR_MIN, Constants.VisionParameters.CUBE_COLOR_MAX, haveCubeSubmat)

        closeArmsContours.clear()
        haveCubeContorus.clear()

        Imgproc.findContours(closeArmsSubmat, closeArmsContours, closeArmsHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)
        Imgproc.findContours(haveCubeSubmat, haveCubeContorus, haveCubeHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)

        closeArms = closeArmsContours.isNotEmpty()
        haveCube = haveCubeContorus.isNotEmpty()
    }
}