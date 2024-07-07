package me.geso.kdary

class DoubleArrayBuilder(
    private val progressFunc: ((Int, Int) -> Unit)?,
) {
    private val units = AutoPool<DoubleArrayBuilderUnit>()
    private val extras = AutoArray<DoubleArrayBuilderExtraUnit>()
    private val labels = AutoPool<UByte>()
    private val table = AutoArray<UInt>()
    private var extrasHead: UInt = 0u

    companion object {
        const val BLOCK_SIZE = 256
        const val NUM_EXTRA_BLOCKS = 16
        const val NUM_EXTRAS = BLOCK_SIZE * NUM_EXTRA_BLOCKS

        const val UPPER_MASK = 0xFF shl 21
        const val LOWER_MASK = 0xFF
    }

    init {
        clear()
    }

    fun clear() {
        units.clear()
        extras.clear()
        labels.clear()
        table.clear()
        extrasHead = 0u
    }

    fun copy(
        sizePtr: IntArray?,
        bufPtr: Array<DoubleArrayBuilderUnit?>?,
    ) {
        sizePtr?.set(0, units.size())
        bufPtr?.let {
            for (i in 0 until units.size()) {
                bufPtr[i] = units[i]
            }
        }
    }

    private fun numBlocks(): Int = units.size() / BLOCK_SIZE

    private fun extras(id: UInt): DoubleArrayBuilderExtraUnit = extras[id.toInt() % NUM_EXTRAS]

    private fun buildFromDawg(dawg: DawgBuilder) {
        var numUnits = 1
        while (numUnits < dawg.size()) {
            numUnits = numUnits shl 1
        }
        units.reserve(numUnits)

        table.reset(Array(dawg.numIntersections()) { 0u })

        extras.reset(Array(NUM_EXTRAS) { DoubleArrayBuilderExtraUnit() })

        reserveId(0u)
        extras(0u).setIsUsed(true)
        units[0].setOffset(1u)
        units[0].setLabel(0u)

        if (dawg.child(dawg.root()) != 0u) {
            buildFromDawg(dawg, dawg.root(), 0u)
        }

        fixAllBlocks()

        extras.clear()
        labels.clear()
        table.clear()
    }

    private fun buildFromDawg(
        dawg: DawgBuilder,
        dawgId: UInt,
        dicId: UInt,
    ) {
        var dawgChildId = dawg.child(dawgId)
        if (dawg.isIntersection(dawgChildId)) {
            val intersectionId = dawg.intersectionId(dawgChildId)
            var offset = table[intersectionId.toInt()]
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

        val offset = arrangeFromDawg(dawg, dawgId, dicId)
        if (dawg.isIntersection(dawgChildId)) {
            table[dawg.intersectionId(dawgChildId).toInt()] = offset
        }

        do {
            val childLabel = dawg.label(dawgChildId)
            val dicChildId = offset xor childLabel.toUInt()
            if (childLabel != 0.toUByte()) {
                buildFromDawg(dawg, dawgChildId, dicChildId)
            }
            dawgChildId = dawg.sibling(dawgChildId)
        } while (dawgChildId != 0u)
    }

    private fun arrangeFromDawg(
        dawg: DawgBuilder,
        dawgId: UInt,
        dicId: UInt,
    ): UInt {
        labels.resize(0)

        var dawgChildId = dawg.child(dawgId)
        while (dawgChildId != 0u) {
            labels.append(dawg.label(dawgChildId))
            dawgChildId = dawg.sibling(dawgChildId)
        }

        val offset = findValidOffset(dicId)
        units[dicId.toInt()].setOffset(dicId xor offset)

        dawgChildId = dawg.child(dawgId)
        for (i in 0 until labels.size()) {
            val dicChildId = offset xor labels[i].toUInt()
            reserveId(dicChildId)
            if (dawg.isLeaf(dawgChildId)) {
                units[dicId.toInt()].setHasLeaf(true)
                units[dicChildId.toInt()].setValue(dawg.value(dawgChildId))
            } else {
                units[dicChildId.toInt()].setLabel(labels[i])
            }
            dawgChildId = dawg.sibling(dawgChildId)
        }
        extras(offset).setIsUsed(true)

        return offset
    }

    private fun findValidOffset(id: UInt): UInt {
        if (extrasHead >= units.size().toUInt()) {
            return units.size().toUInt() or (id and LOWER_MASK.toUInt())
        }

        var unfixedId = extrasHead
        do {
            val offset = unfixedId xor labels[0].toUInt()
            if (isValidOffset(id, offset)) {
                return offset
            }
            unfixedId = extras(unfixedId).next()
        } while (unfixedId != extrasHead)

        return units.size().toUInt() or (id and LOWER_MASK.toUInt())
    }

    private fun isValidOffset(
        id: UInt,
        offset: UInt,
    ): Boolean {
        if (extras(offset).isUsed()) {
            return false
        }

        val relOffset = id xor offset
        if ((relOffset and LOWER_MASK.toUInt()) != 0u && (relOffset and UPPER_MASK.toUInt()) != 0u) {
            return false
        }

        for (i in 1 until labels.size()) {
            if (extras(offset xor labels[i].toUInt()).isFixed()) {
                return false
            }
        }

        return true
    }

    private fun reserveId(id: UInt) {
        if (id >= units.size().toUInt()) {
            expandUnits()
        }

        if (id == extrasHead) {
            extrasHead = extras(id).next()
            if (extrasHead == id) {
                extrasHead = units.size().toUInt()
            }
        }
        extras(extras(id).prev()).setNext(extras(id).next())
        extras(extras(id).next()).setPrev(extras(id).prev())
        extras(id).setIsFixed(true)
    }

    private fun expandUnits() {
        val srcNumUnits = units.size()
        val srcNumBlocks = numBlocks()

        val destNumUnits = srcNumUnits + BLOCK_SIZE
        val destNumBlocks = srcNumBlocks + 1

        if (destNumBlocks > NUM_EXTRA_BLOCKS) {
            fixBlock(srcNumBlocks - NUM_EXTRA_BLOCKS)
        }

        units.resize(destNumUnits)

        if (destNumBlocks > NUM_EXTRA_BLOCKS) {
            for (id in srcNumUnits until destNumUnits) {
                extras(id.toUInt()).setIsUsed(false)
                extras(id.toUInt()).setIsFixed(false)
            }
        }

        for (i in srcNumUnits + 1 until destNumUnits) {
            extras((i - 1).toUInt()).setNext(i.toUInt())
            extras(i.toUInt()).setPrev((i - 1).toUInt())
        }

        extras(srcNumUnits.toUInt()).setPrev((destNumUnits - 1).toUInt())
        extras((destNumUnits - 1).toUInt()).setNext(srcNumUnits.toUInt())

        extras(srcNumUnits.toUInt()).setPrev(extras(extrasHead).prev())
        extras((destNumUnits - 1).toUInt()).setNext(extrasHead)

        extras(extras(extrasHead).prev()).setNext(srcNumUnits.toUInt())
        extras(extrasHead).setPrev((destNumUnits - 1).toUInt())
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

        var unusedOffset: UInt = 0u
        for (offset in begin until end) {
            if (!extras(offset.toUInt()).isUsed()) {
                unusedOffset = offset.toUInt()
                break
            }
        }

        for (id in begin until end) {
            if (!extras(id.toUInt()).isFixed()) {
                reserveId(id.toUInt())
                units[id].setLabel((id xor unusedOffset.toInt()).toUByte())
            }
        }
    }
}
