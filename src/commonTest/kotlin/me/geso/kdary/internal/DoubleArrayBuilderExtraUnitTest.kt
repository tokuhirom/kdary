package me.geso.kdary.internal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DoubleArrayBuilderExtraUnitTest {
    @Test
    fun testSetPrev() {
        val unit = DoubleArrayBuilderExtraUnit()
        unit.setPrev(42u)
        assertEquals(42u, unit.prev())
    }

    @Test
    fun testSetNext() {
        val unit = DoubleArrayBuilderExtraUnit()
        unit.setNext(24u)
        assertEquals(24u, unit.next())
    }

    @Test
    fun testSetIsFixed() {
        val unit = DoubleArrayBuilderExtraUnit()
        unit.setIsFixed(true)
        assertTrue(unit.isFixed())
        unit.setIsFixed(false)
        assertFalse(unit.isFixed())
    }

    @Test
    fun testSetIsUsed() {
        val unit = DoubleArrayBuilderExtraUnit()
        unit.setIsUsed(true)
        assertTrue(unit.isUsed())
        unit.setIsUsed(false)
        assertFalse(unit.isUsed())
    }
}
