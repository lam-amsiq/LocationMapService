package com.lam.locationmapservicelib

import org.junit.Test

class SandboxTest {
    @Test
    fun hashMapConcurrency() {
        val list = arrayListOf(1, 2, 3, 4, 5, 6, 7)
        val map = hashMapOf(
                1 to arrayListOf(3),
                2 to arrayListOf(4),
                3 to arrayListOf(2),
                4 to arrayListOf(),
                5 to arrayListOf(6),
                6 to arrayListOf(),
                7 to arrayListOf())
        val result = arrayListOf<ArrayList<Int>>()
        val remaining = arrayListOf<Int>()
        var group: ArrayList<Int>

        // Print before removal
        println("Before")
        list.forEach { element ->
            println("$element -> ${map[element]}")
        }

        list.forEach { element ->
            map[element]?.let { subList ->
                group = arrayListOf()
                if (subList.isNotEmpty()) {
                    traverseMap(map, element, group, list)
                }

                if (group.isNotEmpty()) {
                    result.add(group)
                } else {
                    remaining.add(element)
                }
            }
        }

        // Print after removal
        println("\nAfter")
        list.forEach { element ->
            println("$element -> ${map[element]}")
        }

        println("\nResult\n$result")
        println("Remaining\n$remaining")
    }

    private fun traverseMap(map: HashMap<Int, ArrayList<Int>>, element: Int, result: ArrayList<Int>, list: ArrayList<Int>) {
        map[element]?.let { subList ->
            result.add(element)
            map.remove(element)

            if (subList.isNotEmpty()) {
                subList.forEach { value ->
                    traverseMap(map, value, result, list)
                }
            }
        }
    }
}