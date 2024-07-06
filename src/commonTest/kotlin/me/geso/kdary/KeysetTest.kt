package me.geso.kdary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class KeysetTest {
    @Test
    fun testKeyset() {
        val keys = arrayOf("apple", "banana", "cherry")
        val lengths = intArrayOf(5, 6, 6)
        val values = arrayOf(1, 2, 3)
        val keyset = Keyset(3, keys, lengths, values)

        assertEquals(3, keyset.numKeys())
        assertEquals("apple", keyset.keys(0))
        assertEquals('b', keyset.keys(1, 0))
        assertEquals(6, keyset.lengths(2))
        assertTrue(keyset.hasLengths())
        assertTrue(keyset.hasValues())
        assertEquals(2, keyset.values(1))

        assertEquals('\u0000', keyset.keys(0, 10)) // Out of bounds
        assertFailsWith<NoSuchElementException> {
            val noValuesKeyset = Keyset<Int>(3, keys, lengths, null)
            noValuesKeyset.values(0)
        }
    }
}
