package me.geso.kdary.internal

import kotlin.test.Test
import kotlin.test.assertEquals

class DoubleArrayBuilderTest {
    @Test
    fun simple() {
        val keyset =
            Keyset(
                keys = arrayOf("abc".encodeToByteArray()),
                values = arrayOf(1),
            )

        val builder = DoubleArrayBuilder()
        val buf = builder.build(keyset)
        buf.forEachIndexed { index, doubleArrayUnit ->
            println("index: $index, unit: $doubleArrayUnit")
        }
        assertEquals(98304u, buf[0].unit)
        assertEquals(98401u, buf[1].unit)
    }

    @Test
    fun build() {
        val keyset =
            Keyset(
                keys = arrayOf("apple", "banana", "cherry").map { it.encodeToByteArray() }.toTypedArray(),
                values = arrayOf(1, 2, 3),
            )

        val builder = DoubleArrayBuilder()
        val buf = builder.build(keyset)
        println(buf.toList())
    }
}
