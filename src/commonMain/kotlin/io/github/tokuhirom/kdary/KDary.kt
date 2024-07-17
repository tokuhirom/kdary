package io.github.tokuhirom.kdary

import io.github.tokuhirom.kdary.internal.DoubleArrayBuilder
import io.github.tokuhirom.kdary.internal.DoubleArrayUnit
import io.github.tokuhirom.kdary.internal.Keyset
import io.github.tokuhirom.kdary.internal.toIdType
import io.github.tokuhirom.kdary.internal.toSizeType
import io.github.tokuhirom.kdary.result.CommonPrefixSearchResult
import io.github.tokuhirom.kdary.result.ExactMatchSearchResult
import io.github.tokuhirom.kdary.result.TraverseResult

/**
 * A callback function to check the progress of dictionary construction.
 * The first argument is the number of processed keys, and the second argument is the total number of keys.
 */
typealias ProgressCallback = (Int, Int) -> Int

class KDary {
    internal val array: Array<DoubleArrayUnit>

    internal constructor(array: Array<DoubleArrayUnit>) {
        check(array.isNotEmpty()) {
            "You can't create a double array with an empty array"
        }

        this.array = array
    }

    /**
     * Returns the number of units.
     *
     * @return The number of units.
     */
    fun size(): Int = array.size

    /**
     * Returns the total size of the array in bytes.
     *
     * @return The total size of the array in bytes.
     */
    fun totalSize(): Int = UNIT_SIZE * size()

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
        nodePos: Int = 0,
    ): ExactMatchSearchResult = exactMatchSearchInternal(key, nodePos)

    private fun exactMatchSearchInternal(
        key: ByteArray,
        nodePosParam: Int = 0,
    ): ExactMatchSearchResult {
        var unit = array[nodePosParam]
        var nodePos = nodePosParam.toSizeType()
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
        maxNumResults: Int? = null,
        nodePos: Int = 0,
    ): List<CommonPrefixSearchResult> = commonPrefixSearchInternal(key, maxNumResults, nodePos)

    private fun commonPrefixSearchInternal(
        key: ByteArray,
        maxNumResults: Int?,
        nodePosParam: Int = 0,
    ): List<CommonPrefixSearchResult> {
        var nodePos = nodePosParam
        val length = key.size

        var unit: DoubleArrayUnit = array[nodePos]
        nodePos = nodePos xor unit.offset().toInt()

        val results = mutableListOf<CommonPrefixSearchResult>()

        for (i in 0 until length) {
            nodePos = (nodePos.toSizeType() xor key[i].toUByte().toSizeType()).toInt()
            unit = array[nodePos]
            if (unit.label() != (key[i].toUByte() and 0xFFU).toIdType()) {
                return results
            }

            nodePos = (nodePos.toSizeType() xor unit.offset().toSizeType()).toInt()
            if (unit.hasLeaf()) {
                if (maxNumResults == null || results.size < maxNumResults) {
                    val v = array[nodePos].value()
                    results.add(CommonPrefixSearchResult(v, i + 1))
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
        nodePos: Int,
        keyPos: Int,
    ): TraverseResult = traverseInternal(key, nodePos, keyPos)

    private fun traverseInternal(
        key: ByteArray,
        nodePosParam: Int,
        keyPosParam: Int,
    ): TraverseResult {
        var id = nodePosParam
        val length = key.size

        var unit = array[id]

        var nodePos = nodePosParam
        var keyPos = keyPosParam

        while (keyPos < length) {
            id = (id.toUInt() xor (unit.offset() xor key[keyPos].toUByte().toUInt())).toInt()
            unit = array[id]
            if (unit.label() != key[keyPos].toUByte().toIdType()) {
                return TraverseResult(-2, nodePos, keyPos)
            }
            nodePos = id

            keyPos++
        }

        return if (!unit.hasLeaf()) {
            TraverseResult(-1, nodePos, keyPos)
        } else {
            unit = array[(id.toUInt() xor unit.offset()).toInt()]
            TraverseResult(unit.value(), nodePos, keyPos)
        }
    }

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
        fun build(
            keys: List<ByteArray>,
            values: List<Int>? = null,
            progressCallback: ProgressCallback? = null,
        ): KDary {
            val keyset = Keyset(keys, values)

            val builder = DoubleArrayBuilder(progressCallback)
            val buf = builder.build(keyset)

            val kdary = KDary(buf)

            val numKeys = keys.size
            progressCallback?.invoke(numKeys + 1, numKeys + 1)

            return kdary
        }

        /**
         * Returns the size of each unit. The size must be 4 bytes.
         *
         * @return The size of each unit.
         */
        internal const val UNIT_SIZE: Int = 4
    }
}
