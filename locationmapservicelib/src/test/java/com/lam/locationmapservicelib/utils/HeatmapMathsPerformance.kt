package com.lam.locationmapservicelib.utils

import com.lam.locationmapservicelib.models.Annotation
import com.lam.locationmapservicelib.models.Location
import org.junit.Assert
import org.junit.Test
import java.util.*

class HeatmapMathsPerformance {
    @Test
    fun computeHashmaps() {
        println("\nBigO computeHashmaps")

        val printAnnotationList = false
        val heatmaps = 223L
        val heatmapSize = 224L
        val annotationList = LinkedList<Annotation>()

        println("heatmaps = $heatmaps")
        println("heatmap size = $heatmapSize")

        for (heatmap in 0 until heatmaps) {
            for (anId in 0 until heatmapSize) {
                annotationList.add(Annotation(anId, "$heatmap.$anId", null, null, Location(anId*10.0 + heatmap * heatmapSize,heatmap * 100.0)))
            }
        }

        println("annotationList size = ${annotationList.size}")
        Assert.assertEquals(annotationList.size.toLong(), heatmaps*heatmapSize)
        if (printAnnotationList) {
            annotationList.forEach { annotation ->
                println(" ${annotation.name}")
            }
        }

        var t0: Long
        var t: Long

        t0 = System.currentTimeMillis()
        HeatmapMaths.computeHeatmaps(annotationList, 10.1, true)
        t = System.currentTimeMillis()

        println("t=${t-t0}")


//        val result = HeatmapMaths.computeHashmaps(annotationList, 10.1, true)
//        result.first.forEach { heatmap ->
//            println("h=$heatmap")
//        }
//        println("a=${result.second}")

        println("BigO computeHeatmaps - SUCCESS")
    }
}