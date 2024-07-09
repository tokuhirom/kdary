package me.geso.kdary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalUnsignedTypes::class)
fun String.toUByteArray(): UByteArray = this.toByteArray().toUByteArray()

@OptIn(ExperimentalUnsignedTypes::class)
class DawgBuilderTest {
    @Test
    fun testHashUnit() {
        val builder = DawgBuilder()
        builder.init()

        builder.insert("abc".toUByteArray(), 3u, 3)
        assertEquals(0u, builder.hashUnit(1u))
    }

    @Test
    fun testSimple() {
        val keyset =
            Keyset(
                1u,
                keys = arrayOf("abc").map { it.toUByteArray() }.toTypedArray(),
                lengths = arrayOf(3u),
                values = arrayOf(1),
            )

        val builder = DawgBuilder()
        builder.init()
        for (i: SizeType in 0uL until keyset.numKeys()) {
            builder.insert(keyset.keys(i), keyset.lengths(i), keyset.values(i))
        }

        for (i: SizeType in 0uL until builder.nodes.size()) {
            println(
                "i: $i, unit=${builder.nodes[i.toInt()].unit()}",
            )
        }

        builder.finish()

        assertEquals(5u, builder.size())
        assertEquals(0u, builder.root())
        for (i: SizeType in 0uL until builder.size()) {
            println(
                "i: $i, unit: ${builder.units[i.toInt()].unit()}," +
                    " label: ${builder.labels[i.toInt()]}," +
                    " is_intersection: ${builder.isIntersection(i.toUInt())}",
            )
        }
        println(builder.units)
        println(builder.units.size())
        println(builder.labels)
        println(builder.isIntersections)
        assertEquals(0, builder.numIntersections())
    }

    @Test
    fun testRoot() {
        val builder = DawgBuilder()
        assertEquals(0u, builder.root())
    }

    @Test
    fun testInit() {
        val builder = DawgBuilder()
        builder.init()
        assertEquals(1u, builder.size())
        assertEquals(0u, builder.root())
        assertEquals(0.toUByte(), builder.label(0u))
        assertEquals(0u, builder.child(0u))
    }

    @Test
    fun testInsert() {
        val builder = DawgBuilder()
        builder.init()
        builder.insert("key".toUByteArray(), 3u, 1)
        // ノード一個なので1が返ってくるはず。
        assertEquals(1u, builder.size())
    }

    @Test
    fun testFinish() {
        val builder = DawgBuilder()
        builder.init()
        builder.insert("key".toUByteArray(), 3u, 1)
        builder.finish()
        assertEquals(5u, builder.size())
        assertFalse(builder.isIntersection(0u))
    }

    @Test
    fun testClear() {
        val builder = DawgBuilder()
        builder.init()
        builder.insert("key".toUByteArray(), 3u, 1)
        builder.clear()
        assertEquals(0u, builder.size())
    }

    @Test
    fun testChild() {
        val builder = DawgBuilder()
        builder.init()
        builder.insert("key".toUByteArray(), 3u, 1)
        assertEquals(0u, builder.child(0u))
    }

    @Test
    fun testSibling() {
        val builder = DawgBuilder()
        builder.init()
        builder.insert("key".toUByteArray(), 3u, 1)
        assertEquals(0u, builder.sibling(0u))
    }

    @Test
    fun testValue() {
        val builder = DawgBuilder()
        builder.init()
        builder.insert("key".toUByteArray(), 3u, 1)
        builder.finish()
        assertEquals(1, builder.value(3u)) // Value set in the last node
    }

    @Test
    fun testIsLeaf() {
        val builder = DawgBuilder()
        builder.init()
        builder.insert("key".toUByteArray(), 3u, 1)
        val leafNodeId = builder.child(0u)
        assertTrue(builder.isLeaf(leafNodeId))
    }

    @Test
    fun testLabel() {
        val builder = DawgBuilder()
        builder.init()
        builder.insert("key".toUByteArray(), 3u, 1)
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
        builder.insert("key".toUByteArray(), 3u, 1)
        builder.finish()
        assertFalse(builder.isIntersection(0u))
    }

    @Test
    fun testIntersectionId() {
        val builder = DawgBuilder()
        builder.init()
        builder.insert("key".toUByteArray(), 3u, 1)
        builder.finish()
        assertEquals(0xFFFFFFFFu, builder.intersectionId(0u))
    }

    @Test
    fun testNumIntersections() {
        val builder = DawgBuilder()
        builder.init()
        builder.insert("key".toUByteArray(), 3u, 1)
        builder.finish()
        assertEquals(0, builder.numIntersections())
    }

    @Test
    fun testSize() {
        val builder = DawgBuilder()
        builder.init()
        builder.insert("key".toUByteArray(), 3u, 1)
        assertEquals(1u, builder.size())
    }
}
