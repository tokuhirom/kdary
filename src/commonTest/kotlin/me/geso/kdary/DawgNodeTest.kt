package me.geso.kdary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DawgNodeTest {
    @Test
    fun testDawgNode() {
        val node = DawgNode()

        node.setChild(1u)
        assertEquals(1u, node.child())

        node.setSibling(2u)
        assertEquals(2u, node.sibling())

        node.setValue(3)
        assertEquals(3, node.value())

        node.setLabel('a'.code.toByte().toUByte())
        assertEquals('a'.code.toByte().toUByte(), node.label())

        node.setIsState(true)
        assertTrue(node.isState())

        node.setHasSibling(true)
        assertTrue(node.hasSibling())

        assertEquals(15u, node.unit()) // (3 << 2) | 2 | 1 = 12 | 2 | 1 = 15

        node.setLabel(0u)
        assertEquals(7u, node.unit()) // (3 << 1) | 1 = 6 | 1 = 7

        node.setIsState(false)
        node.setHasSibling(false)
        assertEquals(6u, node.unit()) // (3 << 1) | 0 = 6
    }
}
