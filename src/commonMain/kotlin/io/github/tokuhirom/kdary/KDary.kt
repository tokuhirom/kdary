package io.github.tokuhirom.kdary

import io.github.tokuhirom.kdary.internal.DoubleArrayBuilder
import io.github.tokuhirom.kdary.internal.DoubleArrayUnit
import io.github.tokuhirom.kdary.internal.Keyset
import io.github.tokuhirom.kdary.internal.toIdType
import io.github.tokuhirom.kdary.internal.toSizeType
import io.github.tokuhirom.kdary.result.CommonPrefixSearchResult
import io.github.tokuhirom.kdary.result.ExactMatchSearchResult
import io.github.tokuhirom.kdary.result.TraverseResult

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
     * Tests whether the given key exists or not. If it exists, the return value is [ExactMatchSearchResult.Found],
     * which contains the value and length. Otherwise, the return value is [ExactMatchSearchResult.NotFound].
     *
     * @param key The key to search for.
     * @param nodePos The starting position of the node.
     * @return An [ExactMatchSearchResult.Found] containing the value and length if the key is found, otherwise [ExactMatchSearchResult.NotFound].
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
        var nodePos = nodePosParam
        val length = key.size
        for (i in 0 until length) {
            nodePos = (nodePos.toSizeType() xor ((unit.offset().toUInt() xor key[i].toUInt()).toULong())).toInt()
            unit = array[nodePos]
            if (unit.label() != key[i].toUInt()) {
                return ExactMatchSearchResult.NotFound
            }
        }

        if (!unit.hasLeaf()) {
            return ExactMatchSearchResult.NotFound
        }
        unit = array[nodePos xor unit.offset()]
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
        nodePos = nodePos xor unit.offset()

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
     * In KDary, a dictionary is a deterministic finite-state automaton (DFA).
     * The `traverse` method tests transitions on the DFA starting from the initial state `nodePos`.
     * It processes the transitions labeled by `key[keyPos]`, `key[keyPos + 1]`, ..., in order.
     *
     * If there is no transition labeled by `key[keyPos + i]`, the method terminates the transitions at that state and returns -2.
     * Otherwise, the method continues without termination and returns -1 or a nonnegative value.
     * -1 indicates that the final state was not an accept state.
     * A nonnegative value indicates the value associated with the final accept state.
     *
     * Note that `traverse` updates `nodePos` and `keyPos` after each transition.
     *
     * @param key The key to traverse.
     * @param nodePos The starting position of the node.
     * @param keyPos The starting position of the key.
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
            id = (id.toUInt() xor (unit.offset().toUInt() xor key[keyPos].toUByte().toUInt())).toInt()
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
            unit = array[(id.toUInt() xor unit.offset().toUInt()).toInt()]
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
            progressCallback: ((Int) -> Unit)? = null,
        ): KDary {
            val keyset = Keyset(keys, values)

            val builder = DoubleArrayBuilder(progressCallback)
            val buf = builder.build(keyset)

            val kdary = KDary(buf)

            val numKeys = keys.size
            progressCallback?.invoke(numKeys + 1)

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
