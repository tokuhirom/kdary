package io.github.tokuhirom.kdary.internal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class DawgBuilderTest {
    @Test
    fun testSimple() {
        val keyset =
            Keyset(
                keys = listOf("abc".encodeToByteArray()),
                values = listOf(1),
            )

        val builder = DawgBuilder()
        for (i in 0 until keyset.numKeys()) {
            builder.insert(keyset.keys(i), keyset.values(i))
        }

        val dawg = builder.finish()

        assertEquals(5, dawg.size())
        assertEquals(0, dawg.root())
        for (i in 0 until dawg.size()) {
            println(
                "i: $i is_intersection: ${dawg.isIntersection(i)}",
            )
        }
        assertEquals(0, dawg.numIntersections())
    }

    @Test
    fun testInsert() {
        val builder = DawgBuilder()
        builder.insert("key".encodeToByteArray(), 1)
        val dawg = builder.finish()
        assertEquals(5, dawg.size())
    }

    @Test
    fun testFinish() {
        val builder = DawgBuilder()
        builder.insert("key".encodeToByteArray(), 1)
        val dawg = builder.finish()
        assertEquals(5, dawg.size())
        assertFalse(dawg.isIntersection(0))
    }

    @Test
    fun testChild() {
        val builder = DawgBuilder()
        builder.insert("key".encodeToByteArray(), 1)
        val dawg = builder.finish()
        assertEquals(4, dawg.child(0))
    }

    @Test
    fun testSibling() {
        val builder = DawgBuilder()
        builder.insert("key".encodeToByteArray(), 1)
        val dawg = builder.finish()
        assertEquals(0, dawg.sibling(0))
    }

    @Test
    fun testValue() {
        val builder = DawgBuilder()
        builder.insert("key".encodeToByteArray(), 1)
        val dawg = builder.finish()
        assertEquals(5, dawg.value(3)) // Value set in the last node
    }

    @Test
    fun testIsLeaf() {
        val builder = DawgBuilder()
        builder.insert("key".encodeToByteArray(), 1)
        val dawg = builder.finish()
        val leafNodeId = dawg.child(0)
        assertFalse(dawg.isLeaf(leafNodeId))
    }

    @Test
    fun testLabel() {
        val builder = DawgBuilder()
        builder.insert("key".encodeToByteArray(), 1)
        val dawg = builder.finish()
        val firstNodeId = dawg.child(0)
        val secondNodeId = dawg.child(firstNodeId)
        val thirdNodeId = dawg.child(secondNodeId)
        assertEquals(107.toUByte(), dawg.label(firstNodeId))
        assertEquals(101.toUByte(), dawg.label(secondNodeId))
        assertEquals(121.toUByte(), dawg.label(thirdNodeId))
    }

    @Test
    fun testIsIntersection() {
        val builder = DawgBuilder()
        builder.insert("key".encodeToByteArray(), 1)
        val dawg = builder.finish()
        assertFalse(dawg.isIntersection(0))
    }

    @Test
    fun testIntersectionId() {
        val builder = DawgBuilder()
        builder.insert("key".encodeToByteArray(), 1)
        val dawg = builder.finish()
        assertEquals(-1, dawg.intersectionId(0))
    }

    @Test
    fun testNumIntersections() {
        val builder = DawgBuilder()
        builder.insert("key".encodeToByteArray(), 1)
        val dawg = builder.finish()
        assertEquals(0, dawg.numIntersections())
    }

    @Test
    fun testSize() {
        val builder = DawgBuilder()
        builder.insert("key".encodeToByteArray(), 1)
        val dawg = builder.finish()
        assertEquals(5, dawg.size())
    }
}
