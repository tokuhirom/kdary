package me.geso.kdary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AutoPoolTest {
    @Test
    fun testAutoPool() {
        val autoPool = AutoPool<Int>()

        // Test isEmpty()
        assertTrue(autoPool.empty())

        // Test pushBack()
        autoPool.pushBack(3)
        autoPool.pushBack(6)
        assertEquals(2u, autoPool.size())
        assertFalse(autoPool.empty())
        assertEquals(3, autoPool[0])
        assertEquals(6, autoPool[1])
        autoPool.popBack()
        assertEquals(2u, autoPool.size())
        assertEquals(3, autoPool[0])
    }
}
