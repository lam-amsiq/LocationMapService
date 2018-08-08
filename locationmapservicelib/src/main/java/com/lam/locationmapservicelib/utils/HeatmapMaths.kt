package com.lam.locationmapservicelib.utils

import com.lam.locationmapservicelib.models.Annotation
import com.lam.locationmapservicelib.models.Location
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

object HeatmapMaths {
    fun computeHeatmaps(annotationList: LinkedList<Annotation>, groupDistance: Double, isSortedByLatitudeAsc: Boolean): Pair<List<List<Annotation>>, List<Annotation>> {
        val annotationMap = HeatmapMaths.mapAnnotations(annotationList, groupDistance, isSortedByLatitudeAsc)
        val heatmaps = arrayListOf<List<Annotation>>()
        val remaining= arrayListOf<Annotation>()

        var heatmap: ArrayList<Annotation>
        annotationList.forEach { annotation ->
            annotationMap[annotation]?.let { pairedAnnotations ->
                heatmap = arrayListOf()
                if (pairedAnnotations.isNotEmpty()) {
                    traverseMap(annotation, heatmap, annotationMap)
                }

                if (heatmap.isNotEmpty()) {
                    heatmaps.add(heatmap)
                } else {
                    remaining.add(annotation)
                }
            }
        }

        return Pair(heatmaps, remaining)
    }

    internal fun traverseMap(annotation: Annotation, heatmap: ArrayList<Annotation>, annotationMap: HashMap<Annotation, ArrayList<Annotation>>) {
        annotationMap[annotation]?.let { pairedAnnotations ->
            heatmap.add(annotation)
            annotationMap.remove(annotation)

            if (pairedAnnotations.isNotEmpty()) {
                pairedAnnotations.forEach { pairedAnnotation ->
                    try {
                        traverseMap(pairedAnnotation, heatmap, annotationMap)
                    } catch (e: StackOverflowError) {

                    }
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

    internal fun getDistance(location1: Location?, location2: Location?): Double? {
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