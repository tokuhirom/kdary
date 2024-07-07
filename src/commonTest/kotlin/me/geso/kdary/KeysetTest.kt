package me.geso.kdary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KeysetTest {
    @Test
    fun testKeyset() {
        val keys = arrayOf("apple", "banana", "cherry")
        val lengths = arrayOf<SizeType>(5u, 6u, 6u)
        val values = arrayOf(1, 2, 3)
        val keyset = Keyset(3u, keys, lengths, values)

        assertEquals(3u, keyset.numKeys())
        assertEquals("apple", keyset.keys(0))
        assertEquals('b', keyset.keys(1u, 0u))
        assertEquals(6u, keyset.lengths(2u))
        assertTrue(keyset.hasLengths())
        assertTrue(keyset.hasValues())
        assertEquals(2, keyset.values(1u))

        assertEquals('\u0000', keyset.keys(0u, 10u)) // Out of bounds
        run {
            val noValuesKeyset = Keyset<Int>(3u, keys, lengths, null)
            assertEquals(10, noValuesKeyset.values(10u))
        }
    }
}
