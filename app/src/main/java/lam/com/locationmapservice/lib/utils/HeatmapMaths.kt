package lam.com.locationmapservice.lib.utils

import lam.com.locationmapservice.lib.models.Annotation
import lam.com.locationmapservice.lib.models.Location
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

object HeatmapMaths {
    fun computeHashmaps(annotationList: LinkedList<Annotation>, groupDistance: Double, isSortedByLatitudeAsc: Boolean): Pair<ArrayList<ArrayList<Annotation>>, ArrayList<Annotation>> {
        val heatmaps = arrayListOf<ArrayList<Annotation>>()
        val remaining: List<Annotation>
        val annotationMap = HeatmapMaths.mapAnnotations(annotationList, groupDistance, isSortedByLatitudeAsc)

        var group: ArrayList<Annotation>
        annotationList.forEach { annotation ->
            annotationMap[annotation]?.let { pairList ->
                if (pairList.isNotEmpty()) {
                    group = arrayListOf()

                    annotationMap.remove(annotation)
                    pairList.forEach { pairedAnnotations ->
                        annotationMap.remove(pairedAnnotations)
                        traverseAnnotationMap(pairedAnnotations, annotationMap, group)
                    }
                    group.add(annotation)
                    group.addAll(pairList)
                    heatmaps.add(group)
                }
            }
        }

        remaining = ArrayList(annotationMap.keys.toList())
        return Pair(heatmaps, remaining)
    }

    private fun traverseAnnotationMap(annotation: Annotation, annotationMap: HashMap<Annotation, ArrayList<Annotation>>, group: ArrayList<Annotation>) {
        annotationMap[annotation]?.let { pairList ->
            annotationMap.remove(annotation)
            if (pairList.isNotEmpty()) {
                group.addAll(pairList)

                pairList.forEach { pairedAnnotations ->
                    traverseAnnotationMap(pairedAnnotations, annotationMap, group)
                }
            }
        }
    }

    internal fun mapAnnotations(annotationList: LinkedList<Annotation>, groupDistance: Double, isSortedByLatitudeAsc: Boolean): HashMap<Annotation, ArrayList<Annotation>> {
        val matchMap = hashMapOf<Annotation, ArrayList<Annotation>>()

        if (!isSortedByLatitudeAsc) {
            //TODO: Sort list by latitude ascending
        }

        var annotationListIterator: MutableListIterator<Annotation>
        var next: Annotation
        annotationList.forEachIndexed { i, current ->
            if (current?.position?.lat == null || current?.position?.lng == null) return@forEachIndexed

            matchMap[current] = arrayListOf()
            annotationListIterator = annotationList.listIterator(i)

            if (annotationListIterator.hasNext()) {
                annotationListIterator.next()
            } else {
                return@forEachIndexed
            }

            while (annotationListIterator.hasNext()) {
                next = annotationListIterator.next() ?: continue

                if (next.position?.lat?.let { nextY ->
                            nextY - current.position?.lat!!
                        } ?: continue <= groupDistance) {

                    HeatmapMaths.getDistance(current.position, next.position)?.let { distance ->
                        if (distance < groupDistance) {
                            matchMap[current]?.add(next)
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