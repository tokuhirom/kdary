package io.github.tokuhirom.kdary.internal

import io.github.tokuhirom.kdary.toSizeType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AutoPoolTest {
    @Test
    fun testAutoPool() {
        val autoPool = mutableListOf<Int>()
        assertTrue(autoPool.isEmpty())
        autoPool.add(3)
        autoPool.add(6)
        assertEquals(2u, autoPool.size.toSizeType())
        assertFalse(autoPool.isEmpty())
        assertEquals(3, autoPool[0])
        assertEquals(6, autoPool[1])
        autoPool.removeLast()
        assertEquals(1u, autoPool.size.toSizeType())
        assertEquals(3, autoPool[0])
        autoPool.add(9)
        assertEquals(2u, autoPool.size.toSizeType())
        assertEquals(3, autoPool[0])
        assertEquals(9, autoPool[1])
    }
}
