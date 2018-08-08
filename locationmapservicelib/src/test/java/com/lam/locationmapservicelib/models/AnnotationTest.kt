package com.lam.locationmapservicelib.models

import org.junit.Test

import org.junit.Assert.*
import java.util.*

class AnnotationTest {
    @Test
    fun jsonStringToAnnotationList() {
        val expected = LinkedList(arrayListOf(
                Annotation(0, "annotation 0", null, null, Location(55.628, 12.082, true)),
                Annotation(1, "annotation 1", null, null, Location(55.6336, 12.0837, true)),
                Annotation(2, "annotation 2", null, null, Location(55.6427, 12.0793, true))
        ))
        val jsonString = "[{\n" +
                "\t\"annotation_id\": 0,\n" +
                "\t\"name\": \"annotation 0\",\n" +
                "\t\"portrait\": null,\n" +
                "\t\"thumb\": null,\n" +
                "\t\"position\": {\n" +
                "\t\t\"lat\": 55.628,\n" +
                "\t\t\"lng\": 12.082,\n" +
                "\t\t\"enabled\": true\n" +
                "\t}\n" +
                "}, {\n" +
                "\t\"annotation_id\": 1,\n" +
                "\t\"name\": \"annotation 1\",\n" +
                "\t\"portrait\": null,\n" +
                "\t\"thumb\": null,\n" +
                "\t\"position\": {\n" +
                "\t\t\"lat\": 55.6336,\n" +
                "\t\t\"lng\": 12.0837,\n" +
                "\t\t\"enabled\": true\n" +
                "\t}\n" +
                "}, {\n" +
                "\t\"annotation_id\": 2,\n" +
                "\t\"name\": \"annotation 2\",\n" +
                "\t\"portrait\": null,\n" +
                "\t\"thumb\": null,\n" +
                "\t\"position\": {\n" +
                "\t\t\"lat\": 55.6427,\n" +
                "\t\t\"lng\": 12.0793,\n" +
                "\t\t\"enabled\": true\n" +
                "\t}\n" +
                "}\n" +
                "]"

        val result = Annotation.jsonStringToAnnotationList(jsonString)
        assertEquals(expected.size, result.size)
    }

    @Test
    fun getAnnotation_id() {
    }

    @Test
    fun getName() {
    }

    @Test
    fun getPortrait() {
    }

    @Test
    fun getThumb() {
    }

    @Test
    fun getPosition() {
    }

    @Test
    fun getMarker_id() {
    }
}