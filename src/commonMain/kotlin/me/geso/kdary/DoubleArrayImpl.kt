@file:OptIn(ExperimentalUnsignedTypes::class)

package me.geso.kdary

import okio.IOException
import okio.buffer
import okio.sink
import okio.source
import java.io.File

private fun readUIntLe(source: okio.BufferedSource): UInt {
    val byte1 = source.readByte().toUInt() and 0xFFU
    val byte2 = source.readByte().toUInt() and 0xFFU
    val byte3 = source.readByte().toUInt() and 0xFFU
    val byte4 = source.readByte().toUInt() and 0xFFU
    return (byte1 or (byte2 shl 8) or (byte3 shl 16) or (byte4 shl 24))
}

private fun writeUIntLe(
    sink: okio.BufferedSink,
    value: UInt,
) {
    sink.writeByte((value and 0xFFU).toInt())
    sink.writeByte(((value shr 8) and 0xFFU).toInt())
    sink.writeByte(((value shr 16) and 0xFFU).toInt())
    sink.writeByte(((value shr 24) and 0xFFU).toInt())
}

class DoubleArrayImpl<T : Number>(
    private var array: Array<DoubleArrayUnit>,
) {
    private var size: SizeType = array.size.toSizeType()

    // TODO sealed class とかにした方がいいかも
    data class ResultPairType<T>(
        var value: T,
        var length: SizeType,
    )

    internal fun array(): Array<DoubleArrayUnit> = array

    /**
     * size() returns the number of units.
     */
    fun size(): SizeType = size

    /**
     * total_size() returns the total size of the array in bytes.
     */
    fun totalSize(): SizeType = unitSize() * size()

    /**
     * Save the double array into the specified file.
     */
    fun save(fileName: String) {
        check(size != 0uL) {
            "You can't save empty array"
        }

        val file = File(fileName)
        file.sink().buffer().use { sink ->
            array.forEach { unit ->
                writeUIntLe(sink, unit.unit)
            }
        }
    }

    /**
     * exactMatchSearch() tests whether the given key exists or not, and
     * if it exists, its value and length are set to `result'.
     * Otherwise, the value and the length of `result' are set to -1 and 0 respectively.
     */
    fun exactMatchSearch(
        key: ByteArray,
        nodePos: SizeType = 0u,
    ): ResultPairType<Int> = exactMatchSearchInternal(key.toUByteArray(), nodePos)

    private fun exactMatchSearchInternal(
        key: UByteArray,
        nodePosParam: SizeType = 0u,
    ): ResultPairType<Int> {
        var unit = array[nodePosParam.toInt()]
        var nodePos = nodePosParam
        val length = key.size.toSizeType()
        for (i in 0uL until length) {
            nodePos = nodePos xor ((unit.offset() xor key[i.toInt()].toUInt()).toULong())
            unit = array[nodePos.toInt()]
            if (unit.label() != key[i.toInt()].toUInt()) {
                return ResultPairType(-1, 0u)
            }
        }

        if (!unit.hasLeaf()) {
            return ResultPairType(-1, 0u)
        }
        unit = array[nodePos.toInt() xor unit.offset().toInt()]
        return ResultPairType(unit.value(), length)
    }

    /**
     * commonPrefixSearch() searches for keys which match a prefix of the given
     * string. If `length' is 0, `key' is handled as a zero-terminated string.
     * The values and the lengths of at most `maxNumResults' matched keys are
     * stored in `results'. commonPrefixSearch() returns the number of matched
     * keys. Note that the return value can be larger than `maxNumResults' if
     * there are more than `maxNumResults' matches. If you want to get all the
     * results, allocate more spaces and call commonPrefixSearch() again.
     */
    fun commonPrefixSearch(
        key: ByteArray,
        maxNumResults: SizeType? = null,
        nodePos: SizeType = 0u,
    ): List<ResultPairType<ValueType>> = commonPrefixSearchInternal(key.toUByteArray(), maxNumResults, nodePos)

    private fun commonPrefixSearchInternal(
        key: UByteArray,
        maxNumResults: SizeType?,
        nodePosParam: SizeType = 0u,
    ): List<ResultPairType<ValueType>> {
        var nodePos: SizeType = nodePosParam
        var length: SizeType = key.size.toSizeType()

        var unit: DoubleArrayUnit = array[nodePos.toInt()]
        nodePos = nodePos xor unit.offset().toSizeType()

        val results = mutableListOf<ResultPairType<ValueType>>()

        for (i in 0uL until length) {
            nodePos = nodePos xor key[i.toInt()].toInt().toSizeType()
            unit = array[nodePos.toInt()]
            if (unit.label() != (key[i.toInt()].toUInt() and 0xFFU)) {
                return results
            }

            nodePos = nodePos xor unit.offset().toSizeType()
            if (unit.hasLeaf()) {
                if (maxNumResults == null || results.size.toULong() < maxNumResults) {
                    val v = array[nodePos.toInt()].value()
                    results.add(ResultPairType(v, (i + 1u)))
                }
            }
        }

        return results
    }

    // In Darts-clone, a dictionary is a deterministic finite-state automaton
    // (DFA) and traverse() tests transitions on the DFA. The initial state is
    // `node_pos' and traverse() chooses transitions labeled key[key_pos],
    // key[key_pos + 1], ... in order. If there is not a transition labeled
    // key[key_pos + i], traverse() terminates the transitions at that state and
    // returns -2. Otherwise, traverse() ends without a termination and returns
    // -1 or a nonnegative value, -1 indicates that the final state was not an
    // accept state. When a nonnegative value is returned, it is the value
    // associated with the final accept state. That is, traverse() returns the
    // value associated with the given key if it exists. Note that traverse()
    // updates `node_pos' and `key_pos' after each transition.
    // TODO use javadoc
    fun traverse(
        key: ByteArray,
        nodePos: SizeType,
        keyPos: SizeType,
        length: SizeType = 0u,
    ): TraverseResult = traverseInternal(key.toUByteArray(), nodePos, keyPos, length)

    private fun traverseInternal(
        key: UByteArray,
        nodePosParam: SizeType,
        keyPosParam: SizeType,
        length: SizeType = 0u,
    ): TraverseResult {
        var id: IdType = nodePosParam.toIdType()

        var unit = array[id.toInt()]

        var nodePos = nodePosParam
        var keyPos = keyPosParam

        if (length != 0uL) {
            while (keyPos < length) {
                id = id xor (unit.offset() xor key[keyPos.toInt()].toUInt())
                unit = array[id.toInt()]
                if (unit.label() != key[keyPos.toInt()].toUInt()) {
                    return TraverseResult(-2, nodePos, keyPos)
                }
                nodePos = id.toSizeType()

                keyPos++
            }
        } else {
            while (key[keyPos.toInt()].toInt() != 0) {
                id = id xor (unit.offset() xor key[keyPos.toInt()].toUInt())
                unit = array[id.toInt()]
                if (unit.label() != key[keyPos.toInt()].toUInt()) {
                    return TraverseResult(-2, nodePos, keyPos)
                }
                nodePos = id.toSizeType()
                keyPos++
            }
        }

        return if (!unit.hasLeaf()) {
            TraverseResult(-1, nodePos, keyPos)
        } else {
            unit = array[(id xor unit.offset()).toInt()]
            TraverseResult(unit.value(), nodePos, keyPos)
        }
    }

    data class TraverseResult(
        val status: Int,
        val nodePos: SizeType? = null,
        val keyPos: SizeType? = null,
    )

    companion object {
        // TODO use javadoc
        // build() constructs a dictionary from given key-value pairs. If `lengths'
        // is NULL, `keys' is handled as an array of zero-terminated strings. If
        // `values' is NULL, the index in `keys' is associated with each key, i.e.
        // the ith key has (i - 1) as its value.
        // Note that the key-value pairs must be arranged in key order and the values
        // must not be negative. Also, if there are duplicate keys, only the first
        // pair will be stored in the resultant dictionary.
        // `progress_func' is a pointer to a callback function. If it is not NULL,
        // it will be called in build() so that the caller can check the progress of
        // dictionary construction. For details, please see the definition of
        // <Darts::Details::progress_func_type>.
        // The return value of build() is 0, and it indicates the success of the
        // operation. Otherwise, build() throws a <Darts::Exception>, which is a
        // derived class of <std::exception>.
        // build() uses another construction algorithm if `values' is not NULL. In
        // this case, Darts-clone uses a Directed Acyclic Word Graph (DAWG) instead
        // of a trie because a DAWG is likely to be more compact than a trie.
        fun <T> build(
            // XXX keys は sorted であること。
            keys: Array<ByteArray>,
            values: Array<T>? = null,
            progressFunc: ProgressFuncType? = null,
        ): DoubleArray {
            val keyset = Keyset(keys.map { it.toUByteArray() }.toTypedArray(), values)

            val builder = DoubleArrayBuilder(progressFunc)
            builder.build(keyset)

            val buf = builder.copy()

            val doubleArray = DoubleArray(buf)

            val numKeys = keys.size.toSizeType()
            progressFunc?.invoke(numKeys + 1u, numKeys + 1u)

            return doubleArray
        }

        /**
         * Read an array of units from the specified file.
         *
         * @param fileName The file name to read.
         * @throws IOException If the file is not found or invalid.
         */
        fun open(fileName: String): DoubleArray {
            val file = File(fileName)
            if (!file.exists()) {
                throw IOException("File not found: $fileName")
            }

            return file.source().buffer().use { source ->
                val actualSize = file.length().toULong()

                val unitSize = unitSize()
                val numUnits = actualSize / unitSize
                if (numUnits < 256uL || numUnits % 256u != 0uL) {
                    throw IOException("numUnits must be 256 or multiple of 256: $numUnits")
                }

                val units = Array(256) { DoubleArrayUnit() }
                for (i in units.indices) {
                    units[i] = DoubleArrayUnit(readUIntLe(source))
                }

                if (units[0].label().toInt().toChar() != '\u0000' ||
                    units[0].hasLeaf() ||
                    units[0].offset() == 0u ||
                    units[0].offset() >= 512u
                ) {
                    throw IOException("Invalid file format")
                }

                for (i in 1 until 256) {
                    if (units[i].label() <= 0xFF.toUInt() && units[i].offset() >= numUnits.toUInt()) {
                        throw IOException("Invalid file format(bad unit)")
                    }
                }

                val buf: Array<DoubleArrayUnit> = Array(numUnits.toInt()) { DoubleArrayUnit() }

                for (i in units.indices) {
                    buf[i] = units[i]
                }

                // TODO: optimize here.
                if (numUnits > 256u) {
                    // 残りのユニットを読み込む
                    for (i in 256uL until numUnits) {
                        buf[i.toInt()] = DoubleArrayUnit(readUIntLe(source))
                    }
                }

                DoubleArrayImpl(buf)
            }
        }

        // unit_size() returns the size of each unit. The size must be 4 bytes.
        private fun unitSize(): SizeType = 4u
    }
}
