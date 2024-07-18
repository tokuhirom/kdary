package io.github.tokuhirom.kdary.internal

import io.github.tokuhirom.kdary.ProgressCallback

/**
 * DAWG -> double-array converter.
 */
internal class DoubleArrayBuilder(
    private val progressCallback: ProgressCallback? = null,
) {
    private val units = mutableListOf<DoubleArrayBuilderUnit>()
    private val extras = Array(NUM_EXTRAS) { DoubleArrayBuilderExtraUnit() }
    private val labels = mutableListOf<UByte>()
    private var extrasHead = 0

    companion object {
        const val BLOCK_SIZE = 256
        const val NUM_EXTRA_BLOCKS = 16
        const val NUM_EXTRAS = BLOCK_SIZE * NUM_EXTRA_BLOCKS

        const val UPPER_MASK = 0xFF shl 21
        const val LOWER_MASK = 0xFF
    }

    fun build(keyset: Keyset): Array<DoubleArrayUnit> {
        if (keyset.hasValues()) {
            val dawg = buildDawg(keyset)
            buildFromDawg(dawg)
        } else {
            buildFromKeyset(keyset)
        }
        return copy()
    }

    private fun copy(): Array<DoubleArrayUnit> =
        (0 until units.size)
            .map {
                DoubleArrayUnit(units[it].unit())
            }.toTypedArray()

    private fun numBlocks(): Int = units.size / BLOCK_SIZE

    private fun extras(id: Int): DoubleArrayBuilderExtraUnit = extras[id % NUM_EXTRAS]

    private fun buildDawg(keyset: Keyset): Dawg {
        val dawgBuilder = DawgBuilder()
        for (i in 0 until keyset.numKeys()) {
            dawgBuilder.insert(keyset.keys(i), keyset.values(i))
            progressCallback?.invoke(i + 1, keyset.numKeys() + 1)
        }
        return dawgBuilder.finish()
    }

    private fun buildFromDawg(dawg: Dawg) {
        var numUnits = 1
        while (numUnits < dawg.size()) {
            numUnits = numUnits shl 1
        }

        val table =
            Array(dawg.numIntersections()) {
                0u
            }

        reserveId(0)
        extras(0).isUsed = true
        units[0].setOffset(1u)
        units[0].setLabel(0u)

        if (dawg.child(dawg.root()) != 0) {
            buildFromDawg(dawg, dawg.root(), 0, table)
        }

        fixAllBlocks()

        labels.clear()
    }

    private fun buildFromDawg(
        dawg: Dawg,
        dawgId: Int,
        dicId: Int,
        table: Array<IdType>,
    ) {
        var dawgChildId = dawg.child(dawgId)
        if (dawg.isIntersection(dawgChildId)) {
            val intersectionId = dawg.intersectionId(dawgChildId)
            var offset: IdType = table[intersectionId]
            if (offset != 0u) {
                offset = offset xor dicId.toUInt()
                if ((offset and UPPER_MASK.toUInt()) == 0u || (offset and LOWER_MASK.toUInt()) == 0u) {
                    if (dawg.isLeaf(dawgChildId)) {
                        units[dicId].setHasLeaf(true)
                    }
                    units[dicId].setOffset(offset)
                    return
                }
            }
        }

        val offset = arrangeFromDawg(dawg, dawgId, dicId)
        if (dawg.isIntersection(dawgChildId)) {
            table[dawg.intersectionId(dawgChildId)] = offset.toUInt()
        }

        do {
            val childLabel: UByte = dawg.label(dawgChildId)
            val dicChildId = (offset.toUInt() xor childLabel.toIdType()).toInt()
            if (childLabel != 0.toUByte()) {
                // TODO dawgChildId を Int に?
                buildFromDawg(dawg, dawgChildId, dicChildId, table)
            }
            dawgChildId = dawg.sibling(dawgChildId)
        } while (dawgChildId != 0)
    }

    private fun arrangeFromDawg(
        dawg: Dawg,
        dawgId: Int,
        dicId: Int,
    ): Int {
        labels.resize(0, 0.toUByte())

        var dawgChildId = dawg.child(dawgId)
        while (dawgChildId != 0) {
            labels.add(dawg.label(dawgChildId))
            dawgChildId = dawg.sibling(dawgChildId)
        }

        val offset = findValidOffset(dicId)
        units[dicId].setOffset(dicId.toUInt() xor offset.toUInt())

        dawgChildId = dawg.child(dawgId)
        for (i in 0 until labels.size) {
            val dicChildId: IdType = offset.toUInt() xor labels[i].toIdType()
            reserveId(dicChildId.toInt())

            if (dawg.isLeaf(dawgChildId)) {
                units[dicId].setHasLeaf(true)
                units[dicChildId.toInt()].setValue(dawg.value(dawgChildId))
            } else {
                units[dicChildId.toInt()].setLabel(labels[i])
            }
            dawgChildId = dawg.sibling(dawgChildId)
        }
        extras(offset).isUsed = true

        return offset
    }

    private fun buildFromKeyset(keyset: Keyset) {
        var numUnits = 1
        while (numUnits < keyset.numKeys()) {
            numUnits = numUnits shl 1
        }

        reserveId(0)
        extras(0).isUsed = true
        units[0].setOffset(1u)
        units[0].setLabel(0u)

        if (keyset.numKeys() > 0L) {
            buildFromKeyset(keyset, 0, keyset.numKeys(), 0, 0)
        }

        fixAllBlocks()

        labels.clear()
    }

    private fun buildFromKeyset(
        keyset: Keyset,
        begin: Int,
        end: Int,
        depth: Int,
        dicId: Int,
    ) {
        val offset = arrangeFromKeyset(keyset, begin, end, depth, dicId)

        var i = begin
        while (i < end) {
            if (keyset.keys(i, depth) != 0.toUByte()) {
                break
            }
            i++
        }
        if (i == end) {
            return
        }

        var lastBegin = i
        var lastLabel: UByte = keyset.keys(i, depth)
        while (++i < end) {
            val label: UByte = keyset.keys(i, depth)
            if (label != lastLabel) {
                buildFromKeyset(keyset, lastBegin, i, depth + 1, (offset.toUInt() xor lastLabel.toIdType()).toInt())
                lastBegin = i
                lastLabel = keyset.keys(i, depth)
            }
        }
        buildFromKeyset(keyset, lastBegin, end, depth + 1, (offset.toUInt() xor lastLabel.toIdType()).toInt())
    }

    private fun arrangeFromKeyset(
        keyset: Keyset,
        begin: Int,
        end: Int,
        depth: Int,
        dicId: Int,
    ): Int {
        labels.resize(0, 0.toUByte())

        var vaue: ValueType = -1
        for (i in begin until end) {
            val label: UByte = keyset.keys(i, depth)
            if (label == 0.toUByte()) {
                if (keyset.values(i) < 0) {
                    throw IllegalArgumentException("failed to build double-array: negative value")
                }

                if (vaue == -1) {
                    vaue = keyset.values(i)
                }
                progressCallback?.invoke(i + 1, keyset.numKeys() + 1)
            }

            if (labels.isEmpty()) {
                labels.add(label)
            } else if (label != labels[labels.size - 1]) {
                if (label < labels[labels.size - 1]) {
                    throw IllegalArgumentException("failed to build double-array: wrong key order")
                }
                labels.add(label)
            }
        }

        val offset: Int = findValidOffset(dicId)
        units[dicId].setOffset(dicId.toUInt() xor offset.toUInt())

        for (i in 0 until labels.size) {
            val dicChildId = (offset xor labels[i].toInt())
            reserveId(dicChildId)
            if (labels[i] == 0.toUByte()) {
                units[dicId].setHasLeaf(true)
                units[dicChildId].setValue(vaue)
            } else {
                units[dicChildId].setLabel(labels[i])
            }
        }
        extras(offset).isUsed = true

        return offset
    }

    private fun findValidOffset(id: Int): Int {
        if (extrasHead >= units.size) {
            return (units.size.toIdType() or (id.toUInt() and LOWER_MASK.toIdType())).toInt()
        }

        var unfixedId = extrasHead
        do {
            val offset: IdType = unfixedId.toUInt() xor labels[0].toIdType()
            if (isValidOffset(id, offset)) {
                return offset.toInt()
            }
            unfixedId = extras(unfixedId).next
        } while (unfixedId != extrasHead)

        return (units.size.toUInt() or (id.toUInt() and LOWER_MASK.toUInt())).toInt()
    }

    private fun isValidOffset(
        id: Int,
        offset: IdType,
    ): Boolean {
        if (extras(offset.toInt()).isUsed) {
            return false
        }

        val relOffset = id.toUInt() xor offset
        if ((relOffset and LOWER_MASK.toUInt()) != 0u && (relOffset and UPPER_MASK.toUInt()) != 0u) {
            return false
        }

        for (i in 1 until labels.size) {
            if (extras((offset xor labels[i].toIdType()).toInt()).isFixed) {
                return false
            }
        }

        return true
    }

    private fun reserveId(id: Int) {
        if (id >= units.size) {
            expandUnits()
        }

        if (id == extrasHead) {
            extrasHead = extras(id).next
            if (extrasHead == id) {
                extrasHead = units.size
            }
        }
        extras(extras(id).prev).next = extras(id).next
        extras(extras(id).next).prev = extras(id).prev
        extras(id).isFixed = true
    }

    private fun expandUnits() {
        val srcNumUnits = units.size
        val srcNumBlocks = numBlocks()

        val destNumUnits = srcNumUnits + BLOCK_SIZE
        val destNumBlocks = srcNumBlocks + 1

        if (destNumBlocks > NUM_EXTRA_BLOCKS) {
            fixBlock(srcNumBlocks - NUM_EXTRA_BLOCKS)
        }

        units.resizeWithBlock(destNumUnits.toSizeType()) {
            DoubleArrayBuilderUnit()
        }

        if (destNumBlocks > NUM_EXTRA_BLOCKS) {
            for (id in srcNumUnits until destNumUnits) {
                extras(id).isUsed = false
                extras(id).isFixed = false
            }
        }

        for (i in srcNumUnits + 1 until destNumUnits) {
            extras(i - 1).next = i
            extras(i).prev = i - 1
        }

        extras(srcNumUnits).prev = destNumUnits - 1
        extras(destNumUnits - 1).next = srcNumUnits

        extras(srcNumUnits).prev = extras(extrasHead).prev
        extras(destNumUnits - 1).next = extrasHead

        extras(extras(extrasHead).prev.toInt()).next = srcNumUnits
        extras(extrasHead).prev = destNumUnits - 1
    }

    private fun fixAllBlocks() {
        var begin = 0
        if (numBlocks() > NUM_EXTRA_BLOCKS) {
            begin = numBlocks() - NUM_EXTRA_BLOCKS
        }
        val end = numBlocks()

        for (blockId in begin until end) {
            fixBlock(blockId)
        }
    }

    private fun fixBlock(blockId: Int) {
        val begin = blockId * BLOCK_SIZE
        val end = begin + BLOCK_SIZE

        var unusedOffset = 0
        for (offset in begin until end) {
            if (!extras(offset).isUsed) {
                unusedOffset = offset
                break
            }
        }

        for (id in begin until end) {
            if (!extras(id).isFixed) {
                reserveId(id)
                units[id].setLabel((id.toIdType() xor unusedOffset.toUInt()).toUByte())
            }
        }
    }
}
