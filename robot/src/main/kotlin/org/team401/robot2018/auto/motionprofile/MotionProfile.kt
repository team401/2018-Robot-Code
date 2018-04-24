package org.team401.robot2018.auto.motionprofile

import com.ctre.phoenix.motion.TrajectoryPoint

/*
 * 2018-Robot-Code - Created on 4/9/18
 * Author: Cameron Earle
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at project root
 */

/**
 * @author Cameron Earle
 * @version 4/9/18
 */

data class MotionProfile(val points: List<Waypoint> = arrayListOf()) {
    private fun pointsAsArrayList() = points as ArrayList<Waypoint>
    private var idx = 0
    var name = ""; private set

    private fun fixHeadings() {
        val adapter = HeadingAdapter()
        val newHeadings = adapter.findNewHeadings(points.map { it.heading })
        newHeadings.forEachIndexed {
            index, heading ->
            points[index].heading = heading
        }
    }

    fun fromCSV(lines: List<String>) {
        val points = pointsAsArrayList()
        points.clear()
        lines.forEach {
            points.add(Waypoint.fromCSVLine(it))
        }
        fixHeadings()
    }

    fun fromPoints(pointsIn: List<Waypoint>, name: String = "") {
        val points = pointsAsArrayList()
        points.clear()
        points.addAll(pointsIn)
        this.name = name
        fixHeadings()
    }

    fun numPoints() = points.size
    fun lastIndex() = points.size - 1

    fun isFirst(idx: Int) = idx == 0
    fun isLast(idx: Int) = idx == lastIndex()

    fun getFirstTimestep() = TrajectoryPoint.TrajectoryDuration.Trajectory_Duration_0ms.valueOf(
            points.getOrNull(0)?.timestep ?: 0
    )

    fun getLastHeading() = points.lastOrNull()?.heading ?: 0.0

    fun getPoint(idx: Int) = points[idx]
}