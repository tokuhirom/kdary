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
        for (i: SizeType in 0uL until keyset.numKeys().toSizeType()) {
            builder.insert(keyset.keys(i.toInt()), keyset.values(i.toInt()))
        }

        for (i: SizeType in 0uL until builder.nodes.size.toSizeType()) {
            println(
                "i: $i, unit=${builder.nodes[i.toInt()].unit()}",
            )
        }

        val dawg = builder.finish()

        assertEquals(5u, dawg.size())
        assertEquals(0, dawg.root())
        for (i: SizeType in 0uL until dawg.size()) {
            println(
                "i: $i, unit: ${builder.units[i.toInt()].unit()}," +
                    " label: ${builder.labels[i.toInt()]}," +
                    " is_intersection: ${dawg.isIntersection(i.toUInt())}",
            )
        }
        assertEquals(0, dawg.numIntersections())
    }

    @Test
    fun testInsert() {
        val builder = DawgBuilder()
        builder.insert("key".encodeToByteArray(), 1)
        val dawg = builder.finish()
        assertEquals(5u, dawg.size())
    }

    @Test
    fun testFinish() {
        val builder = DawgBuilder()
        builder.insert("key".encodeToByteArray(), 1)
        val dawg = builder.finish()
        assertEquals(5u, dawg.size())
        assertFalse(dawg.isIntersection(0u))
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
        assertEquals(0u, dawg.sibling(0u))
    }

    @Test
    fun testValue() {
        val builder = DawgBuilder()
        builder.insert("key".encodeToByteArray(), 1)
        val dawg = builder.finish()
        assertEquals(5, dawg.value(3u)) // Value set in the last node
    }

    @Test
    fun testIsLeaf() {
        val builder = DawgBuilder()
        builder.insert("key".encodeToByteArray(), 1)
        val dawg = builder.finish()
        val leafNodeId = dawg.child(0)
        assertFalse(dawg.isLeaf(leafNodeId.toUInt()))
    }

    @Test
    fun testLabel() {
        val builder = DawgBuilder()
        builder.insert("key".encodeToByteArray(), 1)
        val dawg = builder.finish()
        val firstNodeId = dawg.child(0)
        val secondNodeId = dawg.child(firstNodeId)
        val thirdNodeId = dawg.child(secondNodeId)
        assertEquals(107.toUByte(), dawg.label(firstNodeId.toUInt()))
        assertEquals(101.toUByte(), dawg.label(secondNodeId.toUInt()))
        assertEquals(121.toUByte(), dawg.label(thirdNodeId.toUInt()))
    }

    @Test
    fun testIsIntersection() {
        val builder = DawgBuilder()
        builder.insert("key".encodeToByteArray(), 1)
        val dawg = builder.finish()
        assertFalse(dawg.isIntersection(0u))
    }

    @Test
    fun testIntersectionId() {
        val builder = DawgBuilder()
        builder.insert("key".encodeToByteArray(), 1)
        val dawg = builder.finish()
        assertEquals(0xFFFFFFFFu, dawg.intersectionId(0u))
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
        assertEquals(5u, dawg.size())
    }
}
