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
    private var extrasHead: IdType = 0u

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
        var numUnits: SizeType = 1uL
        while (numUnits < dawg.size()) {
            numUnits = numUnits shl 1
        }

        val table =
            Array(dawg.numIntersections()) {
                0u
            }

        reserveId(0u)
        extras(0).isUsed = true
        units[0].setOffset(1u)
        units[0].setLabel(0u)

        if (dawg.child(dawg.root()) != 0u) {
            buildFromDawg(dawg, dawg.root(), 0u, table)
        }

        fixAllBlocks()

        labels.clear()
    }

    private fun buildFromDawg(
        dawg: Dawg,
        dawgId: IdType,
        dicId: IdType,
        table: Array<IdType>,
    ) {
        var dawgChildId = dawg.child(dawgId)
        if (dawg.isIntersection(dawgChildId)) {
            val intersectionId: IdType = dawg.intersectionId(dawgChildId)
            var offset: IdType = table[intersectionId.toInt()]
            if (offset != 0u) {
                offset = offset xor dicId
                if ((offset and UPPER_MASK.toUInt()) == 0u || (offset and LOWER_MASK.toUInt()) == 0u) {
                    if (dawg.isLeaf(dawgChildId)) {
                        units[dicId.toInt()].setHasLeaf(true)
                    }
                    units[dicId.toInt()].setOffset(offset)
                    return
                }
            }
        }

        val offset: IdType = arrangeFromDawg(dawg, dawgId, dicId)
        if (dawg.isIntersection(dawgChildId)) {
            table[dawg.intersectionId(dawgChildId).toInt()] = offset
        }

        do {
            val childLabel: UByte = dawg.label(dawgChildId)
            val dicChildId: IdType = offset xor childLabel.toIdType()
            if (childLabel != 0.toUByte()) {
                buildFromDawg(dawg, dawgChildId, dicChildId, table)
            }
            dawgChildId = dawg.sibling(dawgChildId)
        } while (dawgChildId != 0u)
    }

    private fun arrangeFromDawg(
        dawg: Dawg,
        dawgId: IdType,
        dicId: IdType,
    ): UInt {
        labels.resize(0uL, 0.toUByte())

        var dawgChildId: IdType = dawg.child(dawgId)
        while (dawgChildId != 0u) {
            labels.add(dawg.label(dawgChildId))
            dawgChildId = dawg.sibling(dawgChildId)
        }

        val offset: IdType = findValidOffset(dicId)
        units[dicId.toInt()].setOffset(dicId xor offset)

        dawgChildId = dawg.child(dawgId)
        for (i in 0 until labels.size) {
            val dicChildId: IdType = offset xor labels[i].toIdType()
            reserveId(dicChildId)

            if (dawg.isLeaf(dawgChildId)) {
                units[dicId.toInt()].setHasLeaf(true)
                units[dicChildId.toInt()].setValue(dawg.value(dawgChildId))
            } else {
                units[dicChildId.toInt()].setLabel(labels[i])
            }
            dawgChildId = dawg.sibling(dawgChildId)
        }
        extras(offset.toInt()).isUsed = true

        return offset
    }

    private fun buildFromKeyset(keyset: Keyset) {
        var numUnits: SizeType = 1uL
        while (numUnits < keyset.numKeys().toSizeType()) {
            numUnits = numUnits shl 1
        }

        reserveId(0u)
        extras(0).isUsed = true
        units[0].setOffset(1u)
        units[0].setLabel(0u)

        if (keyset.numKeys() > 0L) {
            buildFromKeyset(keyset, 0u, keyset.numKeys().toSizeType(), 0u, 0u)
        }

        fixAllBlocks()

        labels.clear()
    }

    private fun buildFromKeyset(
        keyset: Keyset,
        begin: SizeType,
        end: SizeType,
        depth: SizeType,
        dicId: IdType,
    ) {
        val offset: IdType = arrangeFromKeyset(keyset, begin, end, depth, dicId)

        var i: SizeType = begin
        while (i < end) {
            if (keyset.keys(i.toInt(), depth.toInt()) != 0.toUByte()) {
                break
            }
            i++
        }
        if (i == end) {
            return
        }

        var lastBegin: SizeType = i
        var lastLabel: UByte = keyset.keys(i.toInt(), depth.toInt())
        while (++i < end) {
            val label: UByte = keyset.keys(i.toInt(), depth.toInt())
            if (label != lastLabel) {
                buildFromKeyset(keyset, lastBegin, i, depth + 1uL, offset xor lastLabel.toIdType())
                lastBegin = i
                lastLabel = keyset.keys(i.toInt(), depth.toInt())
            }
        }
        buildFromKeyset(keyset, lastBegin, end, depth + 1uL, offset xor lastLabel.toIdType())
    }

    private fun arrangeFromKeyset(
        keyset: Keyset,
        begin: SizeType,
        end: SizeType,
        depth: SizeType,
        dicId: IdType,
    ): IdType {
        labels.resize(0uL, 0.toUByte())

        var vaue: ValueType = -1
        for (i in begin.toInt() until end.toInt()) {
            val label: UByte = keyset.keys(i, depth.toInt())
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

        val offset: IdType = findValidOffset(dicId)
        units[dicId.toInt()].setOffset(dicId xor offset)

        for (i in 0 until labels.size) {
            val dicChildId: IdType = offset xor labels[i].toIdType()
            reserveId(dicChildId)
            if (labels[i] == 0.toUByte()) {
                units[dicId.toInt()].setHasLeaf(true)
                units[dicChildId.toInt()].setValue(vaue)
            } else {
                units[dicChildId.toInt()].setLabel(labels[i])
            }
        }
        extras(offset.toInt()).isUsed = true

        return offset
    }

    private fun findValidOffset(id: IdType): IdType {
        if (extrasHead >= units.size.toSizeType().toIdType()) {
            return units.size.toSizeType().toIdType() or (id and LOWER_MASK.toIdType())
        }

        var unfixedId = extrasHead
        do {
            val offset: IdType = unfixedId xor labels[0].toIdType()
            if (isValidOffset(id, offset)) {
                return offset
            }
            unfixedId = extras(unfixedId.toInt()).next
        } while (unfixedId != extrasHead)

        return units.size.toSizeType().toUInt() or (id and LOWER_MASK.toUInt())
    }

    private fun isValidOffset(
        id: IdType,
        offset: IdType,
    ): Boolean {
        if (extras(offset.toInt()).isUsed) {
            return false
        }

        val relOffset = id xor offset
        if ((relOffset and LOWER_MASK.toUInt()) != 0u && (relOffset and UPPER_MASK.toUInt()) != 0u) {
            return false
        }

        for (i: SizeType in 1uL until labels.size.toSizeType()) {
            if (extras((offset xor labels[i.toInt()].toIdType()).toInt()).isFixed) {
                return false
            }
        }

        return true
    }

    private fun reserveId(id: IdType) {
        if (id >= units.size.toSizeType().toUInt()) {
            expandUnits()
        }

        if (id == extrasHead) {
            extrasHead = extras(id.toInt()).next
            if (extrasHead == id) {
                extrasHead = units.size.toSizeType().toUInt()
            }
        }
        extras(extras(id.toInt()).prev.toInt()).next = extras(id.toInt()).next
        extras(extras(id.toInt()).next.toInt()).prev = extras(id.toInt()).prev
        extras(id.toInt()).isFixed = true
    }

    private fun expandUnits() {
        val srcNumUnits: IdType = units.size.toSizeType().toIdType()
        val srcNumBlocks: IdType = numBlocks().toIdType()

        val destNumUnits: IdType = srcNumUnits + BLOCK_SIZE.toIdType()
        val destNumBlocks: IdType = srcNumBlocks + 1u

        if (destNumBlocks > NUM_EXTRA_BLOCKS.toSizeType()) {
            fixBlock(srcNumBlocks - NUM_EXTRA_BLOCKS.toIdType())
        }

        units.resizeWithBlock(destNumUnits.toSizeType()) {
            DoubleArrayBuilderUnit()
        }

        if (destNumBlocks > NUM_EXTRA_BLOCKS.toIdType()) {
            for (id in srcNumUnits until destNumUnits) {
                extras(id.toInt()).isUsed = false
                extras(id.toInt()).isFixed = false
            }
        }

        for (i: IdType in srcNumUnits + 1u until destNumUnits) {
            extras((i - 1u).toInt()).next = i
            extras(i.toInt()).prev = i - 1u
        }

        extras(srcNumUnits.toInt()).prev = destNumUnits - 1u
        extras((destNumUnits - 1u).toInt()).next = srcNumUnits

        extras(srcNumUnits.toInt()).prev = extras(extrasHead.toInt()).prev
        extras((destNumUnits - 1u).toInt()).next = extrasHead

        extras(extras(extrasHead.toInt()).prev.toInt()).next = srcNumUnits
        extras(extrasHead.toInt()).prev = destNumUnits - 1u
    }

    private fun fixAllBlocks() {
        var begin: IdType = 0u
        if (numBlocks() > NUM_EXTRA_BLOCKS) {
            begin = numBlocks().toIdType() - NUM_EXTRA_BLOCKS.toIdType()
        }
        val end: IdType = numBlocks().toIdType()

        for (blockId: IdType in begin until end) {
            fixBlock(blockId)
        }
    }

    private fun fixBlock(blockId: IdType) {
        val begin: IdType = (blockId * BLOCK_SIZE.toSizeType()).toIdType()
        val end: IdType = (begin + BLOCK_SIZE.toSizeType()).toIdType()

        var unusedOffset: IdType = 0u
        for (offset in begin until end) {
            if (!extras(offset.toInt()).isUsed) {
                unusedOffset = offset
                break
            }
        }

        for (id: IdType in begin until end) {
            if (!extras(id.toInt()).isFixed) {
                reserveId(id)
                units[id.toInt()].setLabel((id xor unusedOffset).toUByte())
            }
        }
    }
}
