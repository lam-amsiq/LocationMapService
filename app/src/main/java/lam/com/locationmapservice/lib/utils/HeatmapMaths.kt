package lam.com.locationmapservice.lib.utils

import lam.com.locationmapservice.lib.models.Annotation
import lam.com.locationmapservice.lib.models.Location
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

object HeatmapMaths {
    fun mapAnnotations(annotationList: LinkedList<Annotation>, groupDistance: Double, isSortedByLatitudeAsc: Boolean): HashMap<Long, ArrayList<Long>> {
        val matchMap = hashMapOf<Long, ArrayList<Long>>()

        if (!isSortedByLatitudeAsc) {
            //TODO: Sort list by latitude ascending
        }

        var annotationListIterator: MutableListIterator<Annotation>
        var next: Annotation
        annotationList.forEachIndexed { i, current ->
            if (current.position?.lat == null || current.position?.lng == null) return@forEachIndexed

            matchMap[current.annotation_id] = arrayListOf()
            annotationListIterator = annotationList.listIterator(i)

            if (annotationListIterator.hasNext()) {
                annotationListIterator.next()
            } else {
                return@forEachIndexed
            }

            while (annotationListIterator.hasNext()) {
                next = annotationListIterator.next()

                if (next.position?.lat?.let { nextY ->
                            nextY - current.position?.lat!!
                        } ?: continue <= groupDistance) {

                    HeatmapMaths.getDistance(current.position, next.position)?.let { distance ->
                        if (distance < groupDistance) {
                            matchMap[current.annotation_id]?.add(next.annotation_id)
                        }
                    }
                } else {
                    break // Break if latitude distance is longer than group distance
                }
            }
        }
        return matchMap
    }

    fun getDistance(location1: Location?, location2: Location?): Double? {
        location1?.lng?.let { x1 ->
            location1.lat?.let { y1 ->
                location2?.lng?.let { x2 ->
                    location2.lat?.let { y2 ->
                        return Math.sqrt(Math.pow(x2 - x1, 2.0) + Math.pow(y2 - y1, 2.0))
                    }
                }
            }
        }
        return null
    }
}