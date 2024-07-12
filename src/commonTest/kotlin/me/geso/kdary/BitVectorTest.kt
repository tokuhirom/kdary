package me.geso.kdary

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
            bitVector.set(i.toSizeType(), i % 2u == 0u)
        }
        bitVector.build()
//        assertEquals(listOf(true, false, true, false, true, false, true, false, true, false), bitVector.toList())
        assertEquals(1u, bitVector.rank(1u))
        assertEquals(2u, bitVector.rank(3u))
        assertEquals(5u, bitVector.rank(9u))
    }

    @Test
    fun testRankEx() {
        val bitVector = BitVector()
        for (i in 0u until 4u) {
            bitVector.append()
        }
        bitVector.set(0u, false)
        bitVector.set(1u, true)
        bitVector.set(2u, false)
        bitVector.set(3u, false)
        bitVector.build()

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
        val bitVector = BitVector()
        for (i in 0u until 10u) {
            bitVector.append()
            bitVector.set(i.toSizeType(), i % 2u == 0u)
        }
        bitVector.build()
        assertEquals(5u, bitVector.numOnes())
        assertEquals(10u, bitVector.size())
    }

    @Test
    fun testIsEmpty() {
        val bitVector = BitVector()
        assertTrue(bitVector.empty)
        bitVector.append()
        assertFalse(bitVector.empty)
    }

    @Test
    fun testClear() {
        val bitVector = BitVector()
        for (i in 0u until 10u) {
            bitVector.append()
            bitVector.set(i.toSizeType(), i % 2u == 0u)
        }
        bitVector.build()
        assertEquals(10u, bitVector.size())
        assertEquals(5u, bitVector.numOnes())

        bitVector.clear()
        assertTrue(bitVector.empty)
        // clear しても size, numOnes は変わらない。これは darts-clone の実装がそうなっている。
//        assertEquals(5u, bitVector.numOnes())
//        assertEquals(10u, bitVector.size())
    }

    @Test
    fun testPopCount() {
        assertEquals(5u, BitVector.Companion.popCount(341u))
    }
}
