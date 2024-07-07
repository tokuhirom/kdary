package me.geso.kdary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AutoPoolTest {
    @Test
    fun testAutoPool() {
        val autoPool = AutoPool<Int>()

        // Test isEmpty()
        assertTrue(autoPool.isEmpty())

        // Test pushBack()
        autoPool.pushBack(1)
        autoPool.pushBack(2)
        autoPool.pushBack(3)
        assertEquals(3, autoPool.size())
        assertFalse(autoPool.isEmpty())
        assertEquals(1, autoPool[0])
        assertEquals(2, autoPool[1])
        assertEquals(3, autoPool[2])

        // Test popBack()
        autoPool.popBack()
        assertEquals(2, autoPool.size())
        assertEquals(1, autoPool[0])
        assertEquals(2, autoPool[1])
        assertFailsWith<IndexOutOfBoundsException> { autoPool[2] }

        // Test append()
        autoPool.append()
        autoPool[2] = 4
        assertEquals(3, autoPool.size())
        assertEquals(1, autoPool[0])
        assertEquals(2, autoPool[1])
        assertEquals(4, autoPool[2])

        // Test append(value: T)
        autoPool.append(5)
        assertEquals(4, autoPool.size())
        assertEquals(5, autoPool[3])

        // Test resize()
        autoPool.resize(6)
        assertEquals(6, autoPool.size())
        assertEquals(1, autoPool[0])
        assertEquals(2, autoPool[1])
        assertEquals(4, autoPool[2])
        assertEquals(5, autoPool[3])
        assertFailsWith<IndexOutOfBoundsException> { autoPool[6] }

        autoPool.resize(4)
        assertEquals(4, autoPool.size())
        assertEquals(1, autoPool[0])
        assertEquals(2, autoPool[1])
        assertEquals(4, autoPool[2])
        assertEquals(5, autoPool[3])

        autoPool.resize(5, 7)
        assertEquals(5, autoPool.size())
        assertEquals(1, autoPool[0])
        assertEquals(2, autoPool[1])
        assertEquals(4, autoPool[2])
        assertEquals(5, autoPool[3])
        assertEquals(7, autoPool[4])

        // Test clear()
        autoPool.clear()
        assertTrue(autoPool.isEmpty())
        assertEquals(0, autoPool.size())
    }
}
