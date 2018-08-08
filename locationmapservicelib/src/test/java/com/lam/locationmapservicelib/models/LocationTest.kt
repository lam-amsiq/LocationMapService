package com.lam.locationmapservicelib.models

import com.google.android.gms.maps.model.LatLng
import org.junit.Test

import org.junit.Assert.*

class LocationTest {
    @Test
    fun toLatLng() {
        var location = Location(0.0, 0.0)
        var expected = LatLng(0.0, 0.0)
        var result = location.toLatLng()
        assertEquals(expected, result)

        location = Location(1.0, 2.0)
        expected = LatLng(1.0, 2.0)
        result = location.toLatLng()
        assertEquals(expected, result)

        location = Location(-1.0, null)
        result = location.toLatLng()
        assertNull(result)
    }

    @Test
    fun lat() {
        var location = Location(0.0, 0.0)
        var expected = 0.0
        var result = location.lat
        assertEquals(expected, result)

        location = Location(1.0, 2.0)
        expected = 1.0
        result = location.lat
        assertEquals(expected, result)

        location = Location(-1.0, null)
        expected = -1.0
        result = location.lat
        assertEquals(expected, result)

        location = Location(null, 1.0)
        result = location.lat
        assertNull(result)
    }

    @Test
    fun lng() {
        var location = Location(0.0, 0.0)
        var expected = 0.0
        var result = location.lng
        assertEquals(expected, result)

        location = Location(2.0, 1.0)
        expected = 1.0
        result = location.lng
        assertEquals(expected, result)

        location = Location(null, -1.0)
        expected = -1.0
        result = location.lng
        assertEquals(expected, result)

        location = Location(1.0, null)
        result = location.lng
        assertNull(result)
    }

    @Test
    fun enabled() {
        var location = Location(0.0, 0.0, false)
        var expected = false
        var result = location.enabled
        assertEquals(expected, result)

        location = Location(0.0, 0.0, true)
        expected = true
        result = location.enabled
        assertEquals(expected, result)

        location = Location(null, 0.0, true)
        expected = true
        result = location.enabled
        assertEquals(expected, result)

        location = Location(0.0, 0.0)
        result = location.enabled
        assertNull(result)
    }
}