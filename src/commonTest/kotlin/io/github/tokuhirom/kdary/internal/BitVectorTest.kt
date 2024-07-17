package io.github.tokuhirom.kdary.internal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BitVectorTest {
    @Test
    fun testSetAndGet() {
        val bitVector = BitVectorBuilder()
        bitVector.append()
        bitVector.set(0u, true)
        assertTrue(bitVector[0u])
        bitVector.set(0u, false)
        assertFalse(bitVector[0u])
    }

    @Test
    fun testRank() {
        val bitVectorBuilder = BitVectorBuilder()
        for (i in 0u until 10u) {
            bitVectorBuilder.append()
            bitVectorBuilder.set(i.toSizeType(), i % 2u == 0u)
        }
        val bitVector = bitVectorBuilder.build()
//        assertEquals(listOf(true, false, true, false, true, false, true, false, true, false), bitVector.toList())
        assertEquals(1u, bitVector.rank(1u))
        assertEquals(2u, bitVector.rank(3u))
        assertEquals(5u, bitVector.rank(9u))
    }

    @Test
    fun testRankEx() {
        val bitVectorBuilder = BitVectorBuilder()
        for (i in 0u until 4u) {
            bitVectorBuilder.append()
        }
        bitVectorBuilder.set(0u, false)
        bitVectorBuilder.set(1u, true)
        bitVectorBuilder.set(2u, false)
        bitVectorBuilder.set(3u, false)
        val bitVector = bitVectorBuilder.build()

        assertEquals(
            listOf(false, true, false, false),
            (0 until 4).map { bitVector[it.toUInt()] },
        )
        assertEquals(
            listOf(0u, 1u, 1u, 1u),
            (0 until 4).map { bitVector.rank(it.toSizeType()) },
        )
    }

    @Test
    fun testNumOnesAndSize() {
        val bitVectorBuilder = BitVectorBuilder()
        for (i in 0u until 10u) {
            bitVectorBuilder.append()
            bitVectorBuilder.set(i.toSizeType(), i % 2u == 0u)
        }
        val bitVector = bitVectorBuilder.build()
        assertEquals(5u, bitVector.numOnes())
    }

    @Test
    fun testClear() {
        val bitVectorBuilder = BitVectorBuilder()
        for (i in 0u until 10u) {
            bitVectorBuilder.append()
            bitVectorBuilder.set(i.toSizeType(), i % 2u == 0u)
        }
        val bitVector = bitVectorBuilder.build()
        assertEquals(5u, bitVector.numOnes())
    }

    @Test
    fun testPopCount() {
        assertEquals(5u, BitVector.Companion.popCount(341u))
    }
}
