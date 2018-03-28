package org.team401.robot2018.auto.motion

import org.snakeskin.factory.ExecutorFactory
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch

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
    class LoadPromise(private val points: ArrayList<Waypoint>) {
        private val latch = CountDownLatch(1)

        fun load(pointsIn: ArrayList<Waypoint>) {
            points.clear()
            points.addAll(pointsIn)
            latch.countDown()
        }

        fun await() {
            latch.await()
        }
    }

    private val cache = ConcurrentHashMap<String, ArrayList<Waypoint>>()
    private val executor = ExecutorFactory.getExecutor("Point loader")

    @Synchronized private fun loadFromFile(filename: String): ArrayList<Waypoint> {
        val f = File(filename)
        val points = arrayListOf<Waypoint>()
        val lines = f.readLines()
        lines.forEach {
            points.add(Waypoint.fromCsv(it))
        }
        println("LOADED $filename DONE ------")
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
        if (!cache.containsKey(profile)) {
            cache.put(profile, loadFromFile(profile))
        }
    }

    /**
     * Preloads a set of profiles, removing any that aren't in the set
     * @param profiles The names of the profiles to load
     */
    fun preloadThese(vararg profiles: String) {
        //Remove any values that shouldn't be in the cache
        val toRemove = arrayListOf<String>()
        cache.forEach {
            key, _ ->
            if (profiles.none { it == key }) {
                toRemove.add(key)
            }
        }
        toRemove.forEach {
            cache.remove(it)
        }
        //Load the cache
        profiles.forEach {
            preload(it)
        }
    }

    /**
     * Populates the input ArrayList immediately from the points in the profile, loading from the cache if possible
     * @param profile The profile to load
     * @param points The ArrayList to load the points into
     */
    fun populateNow(profile: String, points: ArrayList<Waypoint>) {
        points.clear()
        if (cache.containsKey(profile)) {
            points.addAll(cache[profile]!!)
        } else {
            points.addAll(loadFromFile(profile))
        }
    }

    /**
     * Populates the input ArrayList sometime later from the points in the profile, loading from the cache if possible
     * @param profile The profile to load
     * @param points The ArrayList to load the points into
     *
     * @return A promise that tells us when the loading is complete
     */
    fun populateLater(profile: String, points: ArrayList<Waypoint>): LoadPromise {
        val promise = LoadPromise(points)
        executor.submit {
            if (cache.containsKey(profile)) {
                promise.load(cache[profile]!!)
            } else {
                promise.load(loadFromFile(profile))
            }
        }
        return promise
    }
}