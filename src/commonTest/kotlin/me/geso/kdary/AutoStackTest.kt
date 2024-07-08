package me.geso.kdary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AutoStackTest {
    @Test
    fun testAutoStack() {
        val autoStack = AutoStack<Int>()

        // Test isEmpty()
        assertTrue(autoStack.empty())

        // Test push()
        autoStack.push(1)
        autoStack.push(2)
        autoStack.push(3)
        assertEquals(3u, autoStack.size())
        assertFalse(autoStack.empty())
        assertEquals(3, autoStack.top())

        // Test pop()
        autoStack.pop()
        assertEquals(2u, autoStack.size())
        assertEquals(2, autoStack.top())

        autoStack.pop()
        assertEquals(1u, autoStack.size())
        assertEquals(1, autoStack.top())

        autoStack.pop()
        assertEquals(0u, autoStack.size())
        assertTrue(autoStack.empty())
        assertFailsWith<IndexOutOfBoundsException> { autoStack.top() }

        // Test clear()
        autoStack.push(4)
        autoStack.push(5)
        assertEquals(2u, autoStack.size())
        autoStack.clear()
        assertTrue(autoStack.empty())
    }
}
