@file:OptIn(ExperimentalUnsignedTypes::class)

package me.geso.kdary

import kotlin.test.Test

class DoubleArrayBuilderTest {
    @Test
    fun simple() {
        val keyset =
            Keyset(
                1u,
                keys = arrayOf("abc").map { it.toUByteArray() }.toTypedArray(),
                lengths = arrayOf(3u),
                values = arrayOf(1),
            )

        val builder = DoubleArrayBuilder()
        builder.build(keyset)
        val buf: Array<DoubleArrayUnit> = builder.copy()
        buf.forEachIndexed { index, doubleArrayUnit ->
            println("index: $index, unit: $doubleArrayUnit")
        }
    }

    @Test
    fun build() {
        val keyset =
            Keyset(
                3u,
                keys = arrayOf("apple", "banana", "cherry").map { it.toUByteArray() }.toTypedArray(),
                lengths = arrayOf(5u, 6u, 6u),
                values = arrayOf(1, 2, 3),
            )

        val builder = DoubleArrayBuilder()
        builder.build(keyset)
        val buf: Array<DoubleArrayUnit> = builder.copy()
        println(buf.toList())
    }
}