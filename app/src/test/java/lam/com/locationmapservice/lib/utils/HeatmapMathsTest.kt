package lam.com.locationmapservice.lib.utils

import lam.com.locationmapservice.lib.models.Location
import org.junit.Test

import org.junit.Assert.*

class HeatmapMathsTest {
    @Test
    fun getDistance() {
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
    }
}