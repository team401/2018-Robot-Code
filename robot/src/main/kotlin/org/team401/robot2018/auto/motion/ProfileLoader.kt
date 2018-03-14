package org.team401.robot2018.auto.motion

import java.io.File

/*
 * 2018-Robot-Code - Created on 3/13/18
 * Author: Cameron Earle
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at project root
 */

/**
 * @author Cameron Earle
 * @version 3/13/18
 */

object ProfileLoader {
    private val cache = HashMap<String, ArrayList<Waypoint>>()

    private fun loadFromFile(filename: String): ArrayList<Waypoint> {
        val f = File(filename)
        val points = arrayListOf<Waypoint>()
        val lines = f.readLines()
        lines.forEach {
            points.add(Waypoint.fromCsv(it))
        }
        return points
    }

    /**
     * Clears all profiles from the cache, freeing memory
     */
    fun clear() = cache.clear()

    /**
     * Preloads a profile into the cache if it isn't already present
     * @param profile The name of the profile to preload
     */
    fun preload(profile: String) {
        cache.putIfAbsent(profile, loadFromFile(profile))
    }

    /**
     * Populates the input ArrayList from the points in the profile, loading from the cache if possible
     * @param profile The profile to load
     * @param points The ArrayList to load the points into
     */
    fun populate(profile: String, points: ArrayList<Waypoint>) {
        points.clear()
        if (cache.containsKey(profile)) {
            points.addAll(cache[profile]!!)
        } else {
            points.addAll(loadFromFile(profile))
        }
    }
}