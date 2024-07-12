package me.geso.kdary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AutoPoolTest {
    @Test
    fun testAutoPool() {
        val autoPool = mutableListOf<Int>()
        assertTrue(autoPool.isEmpty())
        autoPool.pushBack(3)
        autoPool.pushBack(6)
        assertEquals(2u, autoPool.size())
        assertFalse(autoPool.isEmpty())
        assertEquals(3, autoPool[0])
        assertEquals(6, autoPool[1])
        autoPool.popBack()
        assertEquals(1u, autoPool.size())
        assertEquals(3, autoPool[0])
        autoPool.append(9)
        assertEquals(2u, autoPool.size())
        assertEquals(3, autoPool[0])
        assertEquals(9, autoPool[1])
    }
}
