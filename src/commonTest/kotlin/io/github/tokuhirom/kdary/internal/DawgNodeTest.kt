package io.github.tokuhirom.kdary.internal

import kotlin.test.Test
import kotlin.test.assertEquals

class DawgNodeTest {
    @Test
    fun testDawgNode() {
        val node = DawgNode()

        node.child = 1u
        assertEquals(1u, node.child)

        node.sibling = 2u
        assertEquals(2u, node.sibling)

        node.child = 3.toIdType()
        assertEquals(3, node.child.toValueType())

        node.label = 'a'.code.toByte().toUByte()
        assertEquals('a'.code.toByte().toUByte(), node.label)

        node.isState = true

        node.hasSibling = true

        assertEquals(15u, node.unit()) // (3 << 2) | 2 | 1 = 12 | 2 | 1 = 15

        node.label = 0u
        assertEquals(7u, node.unit()) // (3 << 1) | 1 = 6 | 1 = 7

        node.isState = false
        node.hasSibling = false
        assertEquals(6u, node.unit()) // (3 << 1) | 0 = 6
    }
}
