package me.geso.kdary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DawgBuilderTest {
    @Test
    fun testRoot() {
        val builder = DawgBuilder()
        assertEquals(0u, builder.root())
    }

    @Test
    fun testInit() {
        val builder = DawgBuilder()
        builder.init()
        assertEquals(1, builder.size())
        assertEquals(0u, builder.root())
        assertEquals(0.toUByte(), builder.label(0u))
        assertEquals(0u, builder.child(0u))
    }

    @Test
    fun testInsert() {
        val builder = DawgBuilder()
        builder.init()
        builder.insert("key", 3, 1)
        // ノード一個なので1が返ってくるはず。
        assertEquals(1, builder.size())
    }

    @Test
    fun testFinish() {
        val builder = DawgBuilder()
        builder.init()
        builder.insert("key", 3, 1)
        builder.finish()
        assertEquals(5, builder.size())
        assertFalse(builder.isIntersection(0u))
    }

    @Test
    fun testClear() {
        val builder = DawgBuilder()
        builder.init()
        builder.insert("key", 3, 1)
        builder.clear()
        assertEquals(0, builder.size())
    }

    @Test
    fun testChild() {
        val builder = DawgBuilder()
        builder.init()
        builder.insert("key", 3, 1)
        assertEquals(0u, builder.child(0u))
    }

    @Test
    fun testSibling() {
        val builder = DawgBuilder()
        builder.init()
        builder.insert("key", 3, 1)
        assertEquals(0u, builder.sibling(0u))
    }

    @Test
    fun testValue() {
        val builder = DawgBuilder()
        builder.init()
        builder.insert("key", 3, 1)
        builder.finish()
        assertEquals(1, builder.value(3u)) // Value set in the last node
    }

    @Test
    fun testIsLeaf() {
        val builder = DawgBuilder()
        builder.init()
        builder.insert("key", 3, 1)
        val leafNodeId = builder.child(0u)
        assertTrue(builder.isLeaf(leafNodeId))
    }

    @Test
    fun testLabel() {
        val builder = DawgBuilder()
        builder.init()
        builder.insert("key", 3, 1)
        val firstNodeId = builder.child(0u)
        val secondNodeId = builder.child(firstNodeId)
        val thirdNodeId = builder.child(secondNodeId)
        assertEquals(0.toUByte(), builder.label(firstNodeId))
        assertEquals(0.toUByte(), builder.label(secondNodeId))
        assertEquals(0.toUByte(), builder.label(thirdNodeId))
    }

    @Test
    fun testIsIntersection() {
        val builder = DawgBuilder()
        builder.init()
        builder.insert("key", 3, 1)
        builder.finish()
        assertFalse(builder.isIntersection(0u))
    }

    @Test
    fun testIntersectionId() {
        val builder = DawgBuilder()
        builder.init()
        builder.insert("key", 3, 1)
        builder.finish()
        assertEquals(0xFFFFFFFFu, builder.intersectionId(0u))
    }

    @Test
    fun testNumIntersections() {
        val builder = DawgBuilder()
        builder.init()
        builder.insert("key", 3, 1)
        builder.finish()
        assertEquals(0, builder.numIntersections())
    }

    @Test
    fun testSize() {
        val builder = DawgBuilder()
        builder.init()
        builder.insert("key", 3, 1)
        assertEquals(1, builder.size())
    }
}
