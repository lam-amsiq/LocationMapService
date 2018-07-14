package lam.com.locationmapservice.lib.utils

import lam.com.locationmapservice.lib.models.Location

object HeatmapMaths {
    fun getDistance(location1: Location, location2: Location): Double? {
        location1.lng?.let { x1 ->
            location1.lat?.let { y1 ->
                location2.lng?.let { x2 ->
                    location2.lat?.let { y2 ->
                        return Math.sqrt(Math.pow(x2 - x1, 2.0) + Math.pow(y2 - y1, 2.0))
                    }
                }
            }
        }
        return null
    }
}