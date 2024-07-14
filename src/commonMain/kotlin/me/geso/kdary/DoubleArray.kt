package me.geso.kdary

import me.geso.kdary.internal.DoubleArrayBuilder
import me.geso.kdary.internal.DoubleArrayUnit
import me.geso.kdary.internal.Keyset
import okio.IOException
import okio.buffer
import okio.sink
import okio.source
import java.io.File

/**
 * A callback function to check the progress of dictionary construction.
 * The first argument is the number of processed keys, and the second argument is the total number of keys.
 */
typealias ProgressCallback = (ULong, ULong) -> Int

/**
 * Reads an unsigned 32-bit integer in little-endian format from the given source.
 *
 * @param source The BufferedSource to read from.
 * @return The unsigned 32-bit integer read from the source.
 */
private fun readUIntLe(source: okio.BufferedSource): UInt {
    val byte1 = source.readByte().toUInt() and 0xFFU
    val byte2 = source.readByte().toUInt() and 0xFFU
    val byte3 = source.readByte().toUInt() and 0xFFU
    val byte4 = source.readByte().toUInt() and 0xFFU
    return (byte1 or (byte2 shl 8) or (byte3 shl 16) or (byte4 shl 24))
}

/**
 * Writes an unsigned 32-bit integer in little-endian format to the given sink.
 *
 * @param sink The BufferedSink to write to.
 * @param value The unsigned 32-bit integer to write.
 */
private fun writeUIntLe(
    sink: okio.BufferedSink,
    value: UInt,
) {
    sink.writeByte((value and 0xFFU).toInt())
    sink.writeByte(((value shr 8) and 0xFFU).toInt())
    sink.writeByte(((value shr 16) and 0xFFU).toInt())
    sink.writeByte(((value shr 24) and 0xFFU).toInt())
}

class DoubleArray {
    private val array: Array<DoubleArrayUnit>

    private constructor(array: Array<DoubleArrayUnit>) {
        this.array = array
    }

    sealed class ExactMatchSearchResult(
        open val value: ValueType,
        open val length: SizeType,
    ) {
        data class Found(
            override val value: ValueType,
            override val length: SizeType,
        ) : ExactMatchSearchResult(value, length)

        data object NotFound : ExactMatchSearchResult(-1, 0u)
    }

    data class CommonPrefixSearchResult(
        var value: ValueType,
        var length: SizeType,
    )

    internal fun array(): Array<DoubleArrayUnit> = array

    /**
     * Returns the number of units.
     *
     * @return The number of units.
     */
    fun size(): SizeType = array.size.toSizeType()

    /**
     * Returns the total size of the array in bytes.
     *
     * @return The total size of the array in bytes.
     */
    fun totalSize(): SizeType = unitSize() * size()

    /**
     * Saves the double array into the specified file.
     *
     * @param fileName The name of the file to save to.
     * @throws IllegalStateException If the array is empty.
     */
    fun save(fileName: String) {
        check(array.size.toSizeType() != 0uL) {
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
     * Tests whether the given key exists or not, and if it exists, sets its value and length in the result.
     * Otherwise, the return value is ExactMatchSearchResult.NotFound.
     *
     * @param key The key to search for.
     * @param nodePos The starting position of the node.
     * @return A ExactMatchSearchResult containing the value and length.
     */
    fun exactMatchSearch(
        key: ByteArray,
        nodePos: SizeType = 0u,
    ): ExactMatchSearchResult = exactMatchSearchInternal(key, nodePos)

    private fun exactMatchSearchInternal(
        key: ByteArray,
        nodePosParam: SizeType = 0u,
    ): ExactMatchSearchResult {
        var unit = array[nodePosParam.toInt()]
        var nodePos = nodePosParam
        val length = key.size.toSizeType()
        for (i in 0uL until length) {
            nodePos = nodePos xor ((unit.offset() xor key[i.toInt()].toUInt()).toULong())
            unit = array[nodePos.toInt()]
            if (unit.label() != key[i.toInt()].toUInt()) {
                return ExactMatchSearchResult.NotFound
            }
        }

        if (!unit.hasLeaf()) {
            return ExactMatchSearchResult.NotFound
        }
        unit = array[nodePos.toInt() xor unit.offset().toInt()]
        return ExactMatchSearchResult.Found(unit.value(), length)
    }

    /**
     * Searches for keys which match a prefix of the given string.
     * The values and lengths of matched keys are stored in the results list.
     *
     * @param key The key to search for.
     * @param maxNumResults The maximum number of results to return.
     * @param nodePos The starting position of the node.
     * @return A list of CommonPrefixSearchResult containing the values and lengths of matched keys.
     */
    fun commonPrefixSearch(
        key: ByteArray,
        maxNumResults: SizeType? = null,
        nodePos: SizeType = 0u,
    ): List<CommonPrefixSearchResult> = commonPrefixSearchInternal(key, maxNumResults, nodePos)

    private fun commonPrefixSearchInternal(
        key: ByteArray,
        maxNumResults: SizeType?,
        nodePosParam: SizeType = 0u,
    ): List<CommonPrefixSearchResult> {
        var nodePos: SizeType = nodePosParam
        val length: SizeType = key.size.toSizeType()

        var unit: DoubleArrayUnit = array[nodePos.toInt()]
        nodePos = nodePos xor unit.offset().toSizeType()

        val results = mutableListOf<CommonPrefixSearchResult>()

        for (i in 0uL until length) {
            nodePos = nodePos xor key[i.toInt()].toUByte().toSizeType()
            unit = array[nodePos.toInt()]
            if (unit.label() != (key[i.toInt()].toUByte() and 0xFFU).toIdType()) {
                return results
            }

            nodePos = nodePos xor unit.offset().toSizeType()
            if (unit.hasLeaf()) {
                if (maxNumResults == null || results.size.toULong() < maxNumResults) {
                    val v = array[nodePos.toInt()].value()
                    results.add(CommonPrefixSearchResult(v, (i + 1u)))
                }
            }
        }

        return results
    }

    /**
     * In Darts-clone, a dictionary is a deterministic finite-state automaton (DFA) and traverse() tests transitions on the DFA.
     * The initial state is `nodePos` and traverse() chooses transitions labeled key[keyPos], key[keyPos + 1], ... in order.
     * If there is not a transition labeled key[keyPos + i], traverse() terminates the transitions at that state and returns -2.
     * Otherwise, traverse() ends without a termination and returns -1 or a nonnegative value. -1 indicates that the final state was not an accept state.
     * When a nonnegative value is returned, it is the value associated with the final accept state.
     * That is, traverse() returns the value associated with the given key if it exists. Note that traverse() updates `nodePos` and `keyPos` after each transition.
     *
     * @param key The key to traverse.
     * @param nodePos The starting position of the node.
     * @param keyPos The starting position of the key.
     * @param length The length of the key.
     * @return A TraverseResult containing the status, node position, and key position.
     */
    fun traverse(
        key: ByteArray,
        nodePos: SizeType,
        keyPos: SizeType,
    ): TraverseResult = traverseInternal(key, nodePos, keyPos)

    private fun traverseInternal(
        key: ByteArray,
        nodePosParam: SizeType,
        keyPosParam: SizeType,
    ): TraverseResult {
        var id: IdType = nodePosParam.toIdType()
        val length = key.size.toSizeType()

        var unit = array[id.toInt()]

        var nodePos = nodePosParam
        var keyPos = keyPosParam

        while (keyPos < length) {
            id = id xor (unit.offset() xor key[keyPos.toInt()].toUByte().toUInt())
            unit = array[id.toInt()]
            if (unit.label() != key[keyPos.toInt()].toUByte().toIdType()) {
                return TraverseResult(-2, nodePos, keyPos)
            }
            nodePos = id.toSizeType()

            keyPos++
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
        /**
         * Constructs a dictionary from given key-value pairs.
         * If `values` is NULL, the index in `keys` is associated with each key, i.e., the ith key has (i - 1) as its value.
         * Note that the key-value pairs must be arranged in key order and the values must not be negative.
         * If there are duplicate keys, only the first pair will be stored in the resultant dictionary.
         * The return value of build() is 0, indicating success. Otherwise, build() throws an exception.
         * build() uses a Directed Acyclic Word Graph (DAWG) if `values` is not NULL, as it is likely to be more compact than a trie.
         *
         * @param keys The keys to build the dictionary from.
         * @param values The values associated with the keys.
         * @param progressCallback A callback function to check the progress of dictionary construction.
         * @return A DoubleArray containing the built dictionary.
         */
        fun <T> build(
            keys: Array<ByteArray>,
            values: Array<T>? = null,
            progressCallback: ProgressCallback? = null,
        ): DoubleArray {
            val keyset = Keyset(keys, values)

            val builder = DoubleArrayBuilder(progressCallback)
            val buf = builder.build(keyset)

            val doubleArray = DoubleArray(buf)

            val numKeys = keys.size.toSizeType()
            progressCallback?.invoke(numKeys + 1u, numKeys + 1u)

            return doubleArray
        }

        /**
         * Reads an array of units from the specified file.
         *
         * @param fileName The name of the file to read.
         * @return A DoubleArray containing the read units.
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

                val buf = ByteArray((numUnits - 256u).toInt() * unitSize().toInt())
                source.readFully(buf)

                val doubleArrayUnits: Array<DoubleArrayUnit> = Array(numUnits.toInt()) { DoubleArrayUnit() }

                for (i in units.indices) {
                    doubleArrayUnits[i] = units[i]
                }

                for (i in 0 until (numUnits - 256u).toInt()) {
                    val offset = i * unitSize().toInt()
                    val value = (
                        (buf[offset].toUInt() and 0xFFU) or
                            ((buf[offset + 1].toUInt() and 0xFFU) shl 8) or
                            ((buf[offset + 2].toUInt() and 0xFFU) shl 16) or
                            ((buf[offset + 3].toUInt() and 0xFFU) shl 24)
                    )
                    doubleArrayUnits[i + 256] = DoubleArrayUnit(value)
                }

                DoubleArray(doubleArrayUnits)
            }
        }

        /**
         * Returns the size of each unit. The size must be 4 bytes.
         *
         * @return The size of each unit.
         */
        private fun unitSize(): SizeType = 4u
    }
}
