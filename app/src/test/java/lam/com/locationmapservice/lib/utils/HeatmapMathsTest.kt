package lam.com.locationmapservice.lib.utils

import lam.com.locationmapservice.lib.models.Annotation
import lam.com.locationmapservice.lib.models.Location
import org.junit.Test

import org.junit.Assert.*
import java.util.*

class HeatmapMathsTest {
    @Test
    fun getDistance() {
        println("\nTest getDistance")
        var location1 = Location(0.0, 0.0)
        var location2 = Location(0.0, 0.0)
        var expected = 0.0
        var actual = HeatmapMaths.getDistance(location1, location2)
        assertEquals(expected, actual)

        location1 = Location(-1.0, -0.0)
        location2 = Location(1.0, 0.0)
        expected = 2.0
        actual = HeatmapMaths.getDistance(location1, location2)
        assertEquals(expected, actual)

        location1 = Location(1.0, 0.0)
        location2 = Location(-1.0, -0.0)
        expected = 2.0
        actual = HeatmapMaths.getDistance(location1, location2)
        assertEquals(expected, actual)

        location1 = Location(0.0, -1.0)
        location2 = Location(1.0, 0.0)
        expected = Math.sqrt(2.0)
        actual = HeatmapMaths.getDistance(location1, location2)
        assertEquals(expected, actual)

        location1 = Location(1.4, 2.3)
        location2 = Location(3.2, 4.01)
        expected = 2.4828
        actual = HeatmapMaths.getDistance(location1, location2)
        actual?.let { assertEquals(expected, it, 0.0001) } ?: assertNotNull(actual)

        location1 = Location(1.4, 2.3)
        location2 = Location(null, 4.01)
        actual = HeatmapMaths.getDistance(location1, location2)
        assertNull(actual)

        location1 = Location(1.4, null)
        location2 = Location(0.0, 4.01)
        actual = HeatmapMaths.getDistance(location1, location2)
        assertNull(actual)

        println("Test getDistance - SUCCESS")
    }

    @Test
    fun mapAnnotations() {
        println("\nTest mapAnnotations")

        val a1 = Annotation(1, "Amelia Bones", null, Location(55.725,12.4375))
        val a2 = Annotation(2, "Bellatrix Lestrange", null, Location(55.735,12.437))
        val a3 = Annotation(3, "Cho Chang", null, Location(55.74,12.44))
        val a4 = Annotation(4, "Dolores Umbridge", null, Location(55.72,12.425))
        val a5 = Annotation(5, "Emmeline Vance", null, Location(55.6736,12.5679))
        val a6 = Annotation(6, "Fleur Delacour", null, Location(55.6733,12.5658))
        val a7 = Annotation(7, "Ginny Weasley", null, Location(55.6763,12.5683))
        val a8 = Annotation(8, "Hermione Granger", null, Location(55.6749,12.5641))
        val a9 = Annotation(9, "Irma Pince", null, Location(55.6336,12.0837))
        val a10 = Annotation(10, "Joanne Rowling", null, Location(55.628,12.082))
        val a11 = Annotation(11, "Katie Bell", null, Location(55.6427,12.0793))
        val a12 = Annotation(12, "Luna Lovegood", null, Location(55.6505,12.0789))
        val a13 = Annotation(13, "Minerva McGonagall", null, Location(56.1266,12.3085))

        val annotations = LinkedList(arrayListOf(a10, a9, a11, a12, a6, a5, a8, a7, a4, a1, a2, a3, a13))

        val expected = hashMapOf(
                a1 to arrayListOf(a2,a3),
                a2 to arrayListOf(a3),
                a3 to arrayListOf(),
                a4 to arrayListOf(a1,a2,a3),
                a5 to arrayListOf(a8,a7),
                a6 to arrayListOf(a5,a8,a7),
                a7 to arrayListOf(),
                a8 to arrayListOf(a7),
                a9 to arrayListOf(a11,a12),
                a10 to arrayListOf(a9,a11,a12),
                a11 to arrayListOf(a12),
                a12 to arrayListOf(),
                a13 to arrayListOf())

        val actual = HeatmapMaths.mapAnnotations(annotations, .05, true)

        println("Mapped annotations:")
        actual.forEach { entry ->
            println("${entry.key.annotation_id}->${entry.value}")
        }

        assertEquals(expected, actual)

        println("Test mapAnnotations - SUCCESS")
    }

    @Test
    fun computeHashmaps() {
        println("\nTest computeHashmaps")
        val a1 = Annotation(1, "Amelia Bones", null, Location(55.725,12.4375))
        val a2 = Annotation(2, "Bellatrix Lestrange", null, Location(55.735,12.437))
        val a3 = Annotation(3, "Cho Chang", null, Location(55.74,12.44))
        val a4 = Annotation(4, "Dolores Umbridge", null, Location(55.72,12.425))
        val a5 = Annotation(5, "Emmeline Vance", null, Location(55.6736,12.5679))
        val a6 = Annotation(6, "Fleur Delacour", null, Location(55.6733,12.5658))
        val a7 = Annotation(7, "Ginny Weasley", null, Location(55.6763,12.5683))
        val a8 = Annotation(8, "Hermione Granger", null, Location(55.6749,12.5641))
        val a9 = Annotation(9, "Irma Pince", null, Location(55.6336,12.0837))
        val a10 = Annotation(10, "Joanne Rowling", null, Location(55.628,12.082))
        val a11 = Annotation(11, "Katie Bell", null, Location(55.6427,12.0793))
        val a12 = Annotation(12, "Luna Lovegood", null, Location(55.6505,12.0789))
        val a13 = Annotation(13, "Minerva McGonagall", null, Location(56.1266,12.3085))

        var annotations = LinkedList(arrayListOf(a10, a9, a11, a12, a6, a5, a8, a7, a4, a1, a2, a3, a13))

        var expected = Pair(arrayListOf(
                arrayListOf(a10,a9,a11,a12),
                arrayListOf(a6,a5,a8,a7),
                arrayListOf(a4,a1,a2,a3)
                ), arrayListOf(a13))

        var actual = HeatmapMaths.computeHashmaps(annotations, .05, true)

        println("Heatmaps:")
        actual.first.forEach { heatmap ->
            println(heatmap)
        }
        println("remaining:")
        println(actual.second)

        assertEquals(expected, actual)

        // Check if position is null
        a12.position = null

        expected = Pair(arrayListOf(
                arrayListOf(a10,a9,a11),
                arrayListOf(a6,a5,a8,a7),
                arrayListOf(a4,a1,a2,a3)
        ), arrayListOf(a13))

        actual = HeatmapMaths.computeHashmaps(annotations, .05, true)
        assertEquals(expected, actual)

        // Check if annotation is null
        annotations = LinkedList(arrayListOf(a10, a9, a11, null, a6, a5, a8, a7, a4, a1, a2, a3, a13))

        expected = Pair(arrayListOf(
                arrayListOf(a10,a9,a11),
                arrayListOf(a6,a5,a8,a7),
                arrayListOf(a4,a1,a2,a3)
        ), arrayListOf(a13))

        actual = HeatmapMaths.computeHashmaps(annotations, .05, true)
        assertEquals(expected, actual)

        println("Test computeHeatmaps - SUCCESS")
    }
}