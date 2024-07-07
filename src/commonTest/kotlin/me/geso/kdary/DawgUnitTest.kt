package me.geso.kdary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DawgUnitTest {
    @Test
    fun testDawgUnit() {
        val unit1 = DawgUnit()
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

        val unit3 = DawgUnit(unit2)
        assertEquals(15u, unit3.unit())
        assertEquals(3u, unit3.child())
        assertTrue(unit3.hasSibling())
        assertEquals(7, unit3.value())
        assertTrue(unit3.isState())

        unit3.setUnit(6u)
        assertEquals(6u, unit3.unit())
        assertEquals(1u, unit3.child())
        assertFalse(unit3.hasSibling())
        assertEquals(3, unit3.value())
        assertTrue(unit3.isState())
    }
}