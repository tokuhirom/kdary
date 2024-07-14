package me.geso.kdary.internal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DawgUnitTest {
    @Test
    fun testDawgUnit() {
        val unit1 = DawgUnit(0u)
        assertEquals(0u, unit1.unit())
        assertEquals(0u, unit1.child())
        assertFalse(unit1.hasSibling())
        assertEquals(0, unit1.value())
        assertFalse(unit1.isState())

        val unit2 = DawgUnit(15u)
        assertEquals(15u, unit2.unit())
        assertEquals(3u, unit2.child())
        assertTrue(unit2.hasSibling())
        assertEquals(7, unit2.value())
        assertTrue(unit2.isState())
    }
}
