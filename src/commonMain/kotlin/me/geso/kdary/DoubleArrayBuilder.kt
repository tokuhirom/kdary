@file:OptIn(ExperimentalUnsignedTypes::class)

package me.geso.kdary

class DoubleArrayBuilder(
    private val progressFunc: ProgressFuncType?,
) {
    private val units = AutoPool<DoubleArrayBuilderUnit>()
    private val extras = AutoArray<DoubleArrayBuilderExtraUnit>()
    private val labels = AutoPool<UCharType>()
    private val table = AutoArray<IdType>()
    private var extrasHead: IdType = 0u

    companion object {
        const val BLOCK_SIZE = 256
        const val NUM_EXTRA_BLOCKS = 16
        const val NUM_EXTRAS = BLOCK_SIZE * NUM_EXTRA_BLOCKS

        const val UPPER_MASK = 0xFF shl 21
        const val LOWER_MASK = 0xFF
    }

    fun <T> build(keyset: Keyset<T>) {
        if (keyset.hasValues()) {
            val dawgBuilder = DawgBuilder()
            buildDawg(keyset, dawgBuilder)
            buildFromDawg(dawgBuilder)
            dawgBuilder.clear()
        } else {
            buildFromKeyset(keyset)
        }
    }

    fun copy(
        sizePtr: Array<SizeType>?,
        bufPtr: Array<DoubleArrayBuilderUnit?>?,
    ) {
        TODO("ここは呼び出し元の実装タイミングで実装した方が間違いがなさそう")
    }

    fun clear() {
        units.clear()
        extras.clear()
        labels.clear()
        table.clear()
        extrasHead = 0u
    }

    private fun numBlocks(): SizeType = units.size() / BLOCK_SIZE.toSizeType()

    private fun extras(id: IdType): DoubleArrayBuilderExtraUnit = extras[id.toInt() % NUM_EXTRAS]

    private fun <T> buildDawg(
        keyset: Keyset<T>,
        dawgBuilder: DawgBuilder,
    ) {
        dawgBuilder.init()
        for (i: SizeType in 0uL until keyset.numKeys()) {
            dawgBuilder.insert(keyset.keys(i), keyset.lengths(i), keyset.values(i))
            progressFunc?.invoke(i + 1uL, keyset.numKeys() + 1uL)
        }
        dawgBuilder.finish()
    }

    private fun buildFromDawg(dawg: DawgBuilder) {
        var numUnits: SizeType = 1uL
        while (numUnits < dawg.size()) {
            numUnits = numUnits shl 1
        }
        // reserve は不要
//        units.reserve(numUnits)

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
        dawgId: IdType,
        dicId: IdType,
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
            val childLabel: UCharType = dawg.label(dawgChildId)
            val dicChildId: IdType = offset xor childLabel.toIdType()
            if (childLabel != 0.toUByte()) {
                buildFromDawg(dawg, dawgChildId, dicChildId)
            }
            dawgChildId = dawg.sibling(dawgChildId)
        } while (dawgChildId != 0u)
    }

    private fun arrangeFromDawg(
        dawg: DawgBuilder,
        dawgId: IdType,
        dicId: IdType,
    ): UInt {
        // ここでの resize は不要だと思う。
//        labels.resize(0)

        var dawgChildId: IdType = dawg.child(dawgId)
        while (dawgChildId != 0u) {
            labels.append(dawg.label(dawgChildId))
            dawgChildId = dawg.sibling(dawgChildId)
        }

        val offset: IdType = findValidOffset(dicId)
        units[dicId.toInt()].setOffset(dicId xor offset)

        dawgChildId = dawg.child(dawgId)
        for (i: SizeType in 0uL until labels.size()) {
            val dicChildId: IdType = offset xor labels[i.toInt()].toIdType()
            reserveId(dicChildId)

            if (dawg.isLeaf(dawgChildId)) {
                units[dicId.toInt()].setHasLeaf(true)
                units[dicChildId.toInt()].setValue(dawg.value(dawgChildId))
            } else {
                units[dicChildId.toInt()].setLabel(labels[i.toInt()])
            }
            dawgChildId = dawg.sibling(dawgChildId)
        }
        extras(offset).setIsUsed(true)

        return offset
    }

    private fun <T> buildFromKeyset(keyset: Keyset<T>) {
        var numUnits: SizeType = 1uL
        while (numUnits < keyset.numKeys()) {
            numUnits = numUnits shl 1
        }
        // reserve は不要?
        // units.reserve(numUnits)

        extras.reset(Array(NUM_EXTRAS) { DoubleArrayBuilderExtraUnit() })

        reserveId(0u)
        extras(0u).setIsUsed(true)
        units[0].setOffset(1u)
        units[0].setLabel(0u)

        if (keyset.numKeys() > 0u) {
            // TODO 型をチェック
            buildFromKeyset(keyset, 0u, keyset.numKeys(), 0u, 0u)
        }

        fixAllBlocks()

        extras.clear()
        labels.clear()
    }

    /*
    template <typename T>
void DoubleArrayBuilder::build_from_keyset(const Keyset<T> &keyset,
    std::size_t begin, std::size_t end, std::size_t depth, id_type dic_id) {
  id_type offset = arrange_from_keyset(keyset, begin, end, depth, dic_id);

  while (begin < end) {
    if (keyset.keys(begin, depth) != '\0') {
      break;
    }
    ++begin;
  }
  if (begin == end) {
    return;
  }

  std::size_t last_begin = begin;
  uchar_type last_label = keyset.keys(begin, depth);
  while (++begin < end) {
    uchar_type label = keyset.keys(begin, depth);
    if (label != last_label) {
      build_from_keyset(keyset, last_begin, begin,
          depth + 1, offset ^ last_label);
      last_begin = begin;
      last_label = keyset.keys(begin, depth);
    }
  }
  build_from_keyset(keyset, last_begin, end, depth + 1, offset ^ last_label);
}
     */
    private fun <T> buildFromKeyset(
        keyset: Keyset<T>,
        begin: SizeType,
        end: SizeType,
        depth: SizeType,
        dicId: IdType,
    ) {
        val offset: IdType = arrangeFromKeyset(keyset, begin, end, depth, dicId)

        var i: SizeType = begin
        while (i < end) {
            if (keyset.keys(i, depth) != 0.toUByte()) {
                break
            }
            i++
        }
        if (i == end) {
            return
        }

        var lastBegin: SizeType = i
        var lastLabel: UCharType = keyset.keys(i, depth)
        while (++i < end) {
            val label: UCharType = keyset.keys(i, depth)
            if (label != lastLabel) {
                // TODO ここの XOR での型変換は少し不安
                buildFromKeyset(keyset, lastBegin, i, depth + 1uL, offset xor lastLabel.toIdType())
                lastBegin = i
                lastLabel = keyset.keys(i, depth)
            }
        }
        // TODO ここの XOR での型変換は少し不安
        buildFromKeyset(keyset, lastBegin, end, depth + 1uL, offset xor lastLabel.toIdType())
    }

    private fun <T> arrangeFromKeyset(
        keyset: Keyset<T>,
        begin: SizeType,
        end: SizeType,
        depth: SizeType,
        dicId: IdType,
    ): IdType {
        // ここでの resize は不要だと思う。
//        labels.resize(0)

        var vaue: ValueType = -1
        for (i: SizeType in begin until end) {
            val label: UCharType = keyset.keys(i, depth)
            if (label == 0.toUByte()) {
                if (keyset.hasLengths() && depth < keyset.lengths(i)) {
                    throw IllegalArgumentException("failed to build double-array: invalid null character")
                } else if (keyset.values(i) < 0) {
                    throw IllegalArgumentException("failed to build double-array: negative value")
                }

                if (vaue == -1) {
                    vaue = keyset.values(i)
                }
                progressFunc?.invoke(i + 1uL, keyset.numKeys() + 1uL)
            }

            if (labels.empty()) {
                labels.append(label)
            } else if (label != labels[(labels.size() - 1uL).toInt()]) {
                if (label < labels[(labels.size() - 1uL).toInt()]) {
                    throw IllegalArgumentException("failed to build double-array: wrong key order")
                }
                labels.append(label)
            }
        }

        val offset: IdType = findValidOffset(dicId)
        units[dicId.toInt()].setOffset(dicId xor offset)

        for (i: SizeType in 0uL until labels.size()) {
            // TODO: この xor あってる?
            val dicChildId: IdType = offset xor labels[i.toInt()].toIdType()
            reserveId(dicChildId)
            if (labels[i.toInt()] == 0.toUByte()) {
                units[dicId.toInt()].setHasLeaf(true)
                units[dicChildId.toInt()].setValue(vaue)
            } else {
                units[dicChildId.toInt()].setLabel(labels[i.toInt()])
            }
        }
        extras(offset).setIsUsed(true)

        return offset
    }

    private fun findValidOffset(id: IdType): IdType {
        if (extrasHead >= units.size().toIdType()) {
            return units.size().toIdType() or (id and LOWER_MASK.toIdType())
        }

        var unfixedId = extrasHead
        do {
            val offset: IdType = unfixedId xor labels[0].toIdType()
            if (isValidOffset(id, offset)) {
                return offset
            }
            unfixedId = extras(unfixedId).next()
        } while (unfixedId != extrasHead)

        return units.size().toUInt() or (id and LOWER_MASK.toUInt())
    }

    private fun isValidOffset(
        id: IdType,
        offset: IdType,
    ): Boolean {
        if (extras(offset).isUsed()) {
            return false
        }

        val relOffset = id xor offset
        if ((relOffset and LOWER_MASK.toUInt()) != 0u && (relOffset and UPPER_MASK.toUInt()) != 0u) {
            return false
        }

        for (i: SizeType in 1uL until labels.size()) {
            if (extras(offset xor labels[i.toInt()].toIdType()).isFixed()) {
                return false
            }
        }

        return true
    }

    private fun reserveId(id: IdType) {
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
        val srcNumUnits: IdType = units.size().toIdType()
        val srcNumBlocks: IdType = numBlocks().toIdType()

        val destNumUnits: IdType = srcNumUnits + BLOCK_SIZE.toIdType()
        val destNumBlocks: IdType = srcNumBlocks + 1u

        if (destNumBlocks > NUM_EXTRA_BLOCKS.toSizeType()) {
            fixBlock(srcNumBlocks - NUM_EXTRA_BLOCKS.toIdType())
        }

        // ここでの resize は不要だと思う。
//        units.resize(destNumUnits)

        if (destNumBlocks > NUM_EXTRA_BLOCKS.toIdType()) {
            for (id in srcNumUnits until destNumUnits) {
                extras(id).setIsUsed(false)
                extras(id).setIsFixed(false)
            }
        }

        for (i: IdType in srcNumUnits + 1u until destNumUnits) {
            extras(i - 1u).setNext(i)
            extras(i).setPrev(i - 1u)
        }

        extras(srcNumUnits).setPrev(destNumUnits - 1u)
        extras(destNumUnits - 1u).setNext(srcNumUnits)

        extras(srcNumUnits).setPrev(extras(extrasHead).prev())
        extras(destNumUnits - 1u).setNext(extrasHead)

        extras(extras(extrasHead).prev()).setNext(srcNumUnits)
        extras(extrasHead).setPrev(destNumUnits - 1u)
    }

    private fun fixAllBlocks() {
        var begin: IdType = 0u
        if (numBlocks() > NUM_EXTRA_BLOCKS.toIdType()) {
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
            if (!extras(offset).isUsed()) {
                unusedOffset = offset
                break
            }
        }

        for (id: IdType in begin until end) {
            if (!extras(id).isFixed()) {
                reserveId(id)
                units[id.toInt()].setLabel((id xor unusedOffset).toUByte())
            }
        }
    }
}
