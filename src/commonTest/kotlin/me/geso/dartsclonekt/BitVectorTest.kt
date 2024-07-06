package me.geso.dartsclonekt

import me.geso.kdary.BitVector
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BitVectorTest {
    @Test
    fun testSetAndGet() {
        val bitVector = BitVector()
        bitVector.append()
        bitVector.set(0u, true)
        assertTrue(bitVector[0u])
        bitVector.set(0u, false)
        assertFalse(bitVector[0u])
    }

    @Test
    fun testRank() {
        val bitVector = BitVector()
        for (i in 0u until 10u) {
            bitVector.append()
            bitVector.set(i, i % 2u == 0u)
        }
        bitVector.build()
        assertEquals(listOf(true, false, true, false, true, false, true, false, true, false), bitVector.toList())
        assertEquals(1u, bitVector.rank(1u))
        assertEquals(2u, bitVector.rank(3u))
        assertEquals(5u, bitVector.rank(9u))
    }

    @Test
    fun testNumOnesAndSize() {
        val bitVector = BitVector()
        for (i in 0u until 10u) {
            bitVector.append()
            bitVector.set(i, i % 2u == 0u)
        }
        bitVector.build()
        println(bitVector.toList())
        assertEquals(5u, bitVector.numOnes())
        assertEquals(10u, bitVector.size())
    }

    @Test
    fun testIsEmpty() {
        val bitVector = BitVector()
        assertTrue(bitVector.isEmpty)
        bitVector.append()
        assertFalse(bitVector.isEmpty)
    }

    @Test
    fun testClear() {
        val bitVector = BitVector()
        for (i in 0u until 10u) {
            bitVector.append()
            bitVector.set(i, i % 2u == 0u)
        }
        bitVector.build()
        bitVector.clear()
        assertTrue(bitVector.isEmpty)
        assertEquals(0u, bitVector.numOnes())
        assertEquals(0u, bitVector.size())
    }

    @Test
    fun testPopCount() {
        assertEquals(5u, BitVector.Companion.popCount(341u))
    }
}
