package io.github.tokuhirom.kdary.internal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KeysetTest {
    @Test
    fun testKeyset() {
        val keys = listOf("apple", "banana", "cherry").map { it.encodeToByteArray() }
        val values = listOf(1, 2, 3)
        val keyset = Keyset(keys, values)

        assertEquals(3u, keyset.numKeys())
        assertEquals("apple", keyset.keys(0u).decodeToString())
        assertEquals('b'.code.toUByte(), keyset.keys(1u, 0u))
        assertTrue(keyset.hasValues())
        assertEquals(2, keyset.values(1u))

        assertEquals(0.toUByte(), keyset.keys(0u, 10u)) // Out of bounds
        run {
            val noValuesKeyset = Keyset<Int>(keys, null)
            assertEquals(10, noValuesKeyset.values(10u))
        }
    }
}
