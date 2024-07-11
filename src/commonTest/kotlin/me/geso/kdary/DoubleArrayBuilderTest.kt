@file:OptIn(ExperimentalUnsignedTypes::class)

package me.geso.kdary

import kotlin.test.Test
import kotlin.test.assertEquals

class DoubleArrayBuilderTest {
    @Test
    fun simple() {
        val keyset =
            Keyset(
                keys = arrayOf("abc").map { it.toUByteArray() }.toTypedArray(),
                values = arrayOf(1),
            )

        val builder = DoubleArrayBuilder()
        builder.build(keyset)
        val buf: Array<DoubleArrayUnit> = builder.copy()
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
                keys = arrayOf("apple", "banana", "cherry").map { it.toUByteArray() }.toTypedArray(),
                values = arrayOf(1, 2, 3),
            )

        val builder = DoubleArrayBuilder()
        builder.build(keyset)
        val buf: Array<DoubleArrayUnit> = builder.copy()
        println(buf.toList())
    }
}
