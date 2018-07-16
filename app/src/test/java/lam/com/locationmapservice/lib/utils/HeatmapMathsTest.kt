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
        val annotations = LinkedList(arrayListOf(
                Annotation(10, "Joanne Rowling", null, Location(55.628,12.082)),
                Annotation(9, "Irma Pince", null, Location(55.6336,12.0837)),
                Annotation(11, "Katie Bell", null, Location(55.6427,12.0793)),
                Annotation(12, "Luna Lovegood", null, Location(55.6505,12.0789)),
                Annotation(6, "Fleur Delacour", null, Location(55.6733,12.5658)),
                Annotation(5, "Emmeline Vance", null, Location(55.6736,12.5679)),
                Annotation(8, "Hermione Granger", null, Location(55.6749,12.5641)),
                Annotation(7, "Ginny Weasley", null, Location(55.6763,12.5683)),
                Annotation(4, "Dolores Umbridge", null, Location(55.72,12.425)),
                Annotation(1, "Amelia Bones", null, Location(55.725,12.4375)),
                Annotation(2, "Bellatrix Lestrange", null, Location(55.735,12.437)),
                Annotation(3, "Cho Chang", null, Location(55.74,12.44)),
                Annotation(13, "Minerva McGonagall", null, Location(56.1266,12.3085))))

        val expected = hashMapOf(
                1L to arrayListOf(2L,3L),
                2L to arrayListOf(3L),
                3L to arrayListOf(),
                4L to arrayListOf(1L,2L,3L),
                5L to arrayListOf(8L,7L),
                6L to arrayListOf(5L,8L,7L),
                7L to arrayListOf(),
                8L to arrayListOf(7L),
                9L to arrayListOf(11L,12L),
                10L to arrayListOf(9L,11L,12L),
                11L to arrayListOf(12L),
                12L to arrayListOf(),
                13L to arrayListOf())

        val actual = HeatmapMaths.mapAnnotations(annotations, .05, true)

        println("Mapped annotations:")
        actual.forEach { entry ->
            println("${entry.key}->${entry.value}")
        }

        assertEquals(expected, actual)

        println("Test mapAnnotations - SUCCESS")
    }

    @Test
    fun computeHashmaps() {
        println("\nTest computeHashmaps")
        val annotations = LinkedList(arrayListOf(
                Annotation(10, "Joanne Rowling", null, Location(55.628,12.082)),
                Annotation(9, "Irma Pince", null, Location(55.6336,12.0837)),
                Annotation(11, "Katie Bell", null, Location(55.6427,12.0793)),
                Annotation(12, "Luna Lovegood", null, Location(55.6505,12.0789)),
                Annotation(6, "Fleur Delacour", null, Location(55.6733,12.5658)),
                Annotation(5, "Emmeline Vance", null, Location(55.6736,12.5679)),
                Annotation(8, "Hermione Granger", null, Location(55.6749,12.5641)),
                Annotation(7, "Ginny Weasley", null, Location(55.6763,12.5683)),
                Annotation(4, "Dolores Umbridge", null, Location(55.72,12.425)),
                Annotation(1, "Amelia Bones", null, Location(55.725,12.4375)),
                Annotation(2, "Bellatrix Lestrange", null, Location(55.735,12.437)),
                Annotation(3, "Cho Chang", null, Location(55.74,12.44)),
                Annotation(13, "Minerva McGonagall", null, Location(56.1266,12.3085))))

        val expected = Pair(arrayListOf(
                arrayListOf(10L,9L,11L,12L),
                arrayListOf(6L,5L,8L,7L),
                arrayListOf(4L,1L,2L,3L)
                ), arrayListOf(13L))

        val actual = HeatmapMaths.computeHashmaps(annotations, .05, true)

        println("Heatmaps:")
        actual.first.forEach { heatmap ->
            println(heatmap)
        }
        println("remaining:")
        println(actual.second)

        assertEquals(expected, actual)

        println("Test computeHeatmaps - SUCCESS")
    }
}