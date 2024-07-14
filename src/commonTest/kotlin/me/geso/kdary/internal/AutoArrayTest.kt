package me.geso.kdary.internal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class AutoArrayTest {
    @Test
    fun testAutoArray() {
        val array = arrayOf(1, 2, 3)
        val autoArray = AutoArray(array)

        // Test get and set
        assertEquals(1, autoArray[0])
        assertEquals(2, autoArray[1])
        assertEquals(3, autoArray[2])

        autoArray[0] = 10
        assertEquals(10, autoArray[0])

        // Test isEmpty
        assertTrue(!autoArray.isEmpty())

        // Test clear
        autoArray.clear()
        assertTrue(autoArray.isEmpty())
        assertFailsWith<IndexOutOfBoundsException> { autoArray[0] }

        // Test reset
        autoArray.reset(arrayOf(4, 5, 6))
        assertEquals(4, autoArray[0])
        assertEquals(5, autoArray[1])
        assertEquals(6, autoArray[2])
    }
}