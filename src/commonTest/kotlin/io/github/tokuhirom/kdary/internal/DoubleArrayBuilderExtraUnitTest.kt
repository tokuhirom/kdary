package io.github.tokuhirom.kdary.internal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DoubleArrayBuilderExtraUnitTest {
    @Test
    fun testSetPrev() {
        val unit = DoubleArrayBuilderExtraUnit()
        unit.prev = 42
        assertEquals(42, unit.prev)
    }

    @Test
    fun testSetNext() {
        val unit = DoubleArrayBuilderExtraUnit()
        unit.next = 24
        assertEquals(24, unit.next)
    }

    @Test
    fun testSetIsFixed() {
        val unit = DoubleArrayBuilderExtraUnit()
        unit.isFixed = true
        assertTrue(unit.isFixed)
        unit.isFixed = false
        assertFalse(unit.isFixed)
    }

    @Test
    fun testSetIsUsed() {
        val unit = DoubleArrayBuilderExtraUnit()
        unit.isUsed = true
        assertTrue(unit.isUsed)
        unit.isUsed = false
        assertFalse(unit.isUsed)
    }
}
