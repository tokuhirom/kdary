// Darts.kt

package darts

import java.lang.RuntimeException
import kotlin.Exception

const val DARTS_VERSION = "0.32"

// DARTS_THROW() throws a <Darts.Exception> whose message starts with the
// file name and the line number. For example, DARTS_THROW("error message") at
// line 123 of "darts.h" throws a <Darts.Exception> which has a pointer to
// "darts.h:123: exception: error message". The message is available by using
// what() as well as that of <std::exception>.
fun DARTS_THROW(msg: String): Nothing = throw DartsException(msg)

class DartsException(message: String): Exception(message)

object Darts {

    // The following namespace hides the internal types and classes.
    private object Details {

        // This header assumes that <int> and <unsigned int> are 32-bit integer types.
        //
        // Darts-clone keeps values associated with keys. The type of the values is
        // <value_type>. Note that the values must be positive integers because the
        // most significant bit (MSB) of each value is used to represent whether the
        // corresponding unit is a leaf or not. Also, the keys are represented by
        // sequences of <char_type>s. <uchar_type> is the unsigned type of <char_type>.
        typealias CharType = Char
        typealias UCharType = UByte
        typealias ValueType = Int

        // The main structure of Darts-clone is an array of <DoubleArrayUnit>s, and the
        // unit type is actually a wrapper of <id_type>.
        typealias IdType = UInt

        // <progress_func_type> is the type of callback functions for reporting the
        // progress of building a dictionary. See also build() of <DoubleArray>.
        // The 1st argument receives the progress value and the 2nd argument receives
        // the maximum progress value. A usage example is to show the progress
        // percentage, 100.0 * (the 1st argument) / (the 2nd argument).
        typealias ProgressFuncType = (size_t: Int, size_t: Int) -> Int

        // <DoubleArrayUnit> is the type of double-array units and it is a wrapper of
        // <id_type> in practice.
        class DoubleArrayUnit {
            private var unit: IdType = 0u

            // has_leaf() returns whether a leaf unit is immediately derived from the
            // unit (true) or not (false).
            fun hasLeaf(): Boolean {
                return ((unit.toInt() shr 8) and 1) == 1
            }

            // value() returns the value stored in the unit, and thus value() is
            // available when and only when the unit is a leaf unit.
            fun value(): ValueType {
                return (unit and ((1u shl 31) - 1u)).toInt()
            }

            // label() returns the label associted with the unit. Note that a leaf unit
            // always returns an invalid label. For this feature, leaf unit's label()
            // returns an <id_type> that has the MSB of 1.
            fun label(): IdType {
                return unit and ((1u shl 31) or 0xFFu)
            }

            // offset() returns the offset from the unit to its derived units.
            fun offset(): IdType {
                return (unit shr 10) shl ((unit and (1u shl 9)) shr 6)
            }
        }

        // Darts-clone throws an <Exception> for memory allocation failure, invalid
        // arguments or a too large offset. The last case means that there are too many
        // keys in the given set of keys. Note that the `msg' of <Exception> must be a
        // constant or static string because an <Exception> keeps only a pointer to
        // that string.
        class Exception(msg: String?) : RuntimeException(msg) {
            override val message: String? = msg ?: ""
        }

        class AutoArray<T>(private var array: Array<T>? = null) {
            fun isEmpty(): Boolean = array == null

            fun clear() {
                array = null
            }

            fun swap(other: AutoArray<T>) {
                val temp = array
                array = other.array
                other.array = temp
            }

            fun reset(newArray: Array<T>? = null) {
                AutoArray(newArray).swap(this)
            }

            operator fun get(id: Int): T {
                return array!![id]
            }

            operator fun set(id: Int, value: T) {
                array!![id] = value
            }
        }

        // Memory management of resizable array.
        class AutoPool<T> {
            private var buf: AutoArray<Byte> = AutoArray()
            private var size: Int = 0
            private var capacity: Int = 0

            fun isEmpty(): Boolean = size == 0
            fun size(): Int = size

            fun clear() {
                resize(0)
                buf.clear()
                size = 0
                capacity = 0
            }

            operator fun get(id: Int): T {
                return buf[id] as T
            }

            operator fun set(id: Int, value: T) {
                buf[id] = value as Byte
            }

            fun pushBack(value: T) {
                append(value)
            }

            fun popBack() {
                size--
            }

            fun append() {
                if (size == capacity) resizeBuf(size + 1)
                size++
            }

            fun append(value: T) {
                if (size == capacity) resizeBuf(size + 1)
                size++
            }

            fun resize(newSize: Int) {
                while (size > newSize) {
                    size--
                }
                if (newSize > capacity) {
                    resizeBuf(newSize)
                }
                while (size < newSize) {
                    size++
                }
            }

            fun reserve(newSize: Int) {
                if (newSize > capacity) {
                    resizeBuf(newSize)
                }
            }

            private fun resizeBuf(newSize: Int) {
                var newCapacity = capacity
                if (newSize >= newCapacity * 2) {
                    newCapacity = newSize
                } else {
                    newCapacity = 1
                    while (newCapacity < newSize) {
                        newCapacity = newCapacity shl 1
                    }
                }

                val newBuf = AutoArray<Byte>()
                newBuf.reset(Array(newCapacity) { 0.toByte() })

                if (size > 0) {
                    val src = buf
                    val dest = newBuf
                    for (i in 0 until size) {
                        dest[i] = src[i]
                    }
                }

                buf.swap(newBuf)
                capacity = newCapacity
            }
        }

        class BitVector {
            private var units: AutoPool<IdType> = AutoPool()
            private var ranks: AutoArray<IdType> = AutoArray()
            private var numOnes: Int = 0
            private var size: Int = 0

            operator fun get(id: Int): Boolean {
                return (units[id / UNIT_SIZE] shr (id % UNIT_SIZE) and 1u) == 1u
            }

            fun rank(id: Int): IdType {
                val unitId = id / UNIT_SIZE
                return ranks[unitId] + popCount(units[unitId] and (~0u shr (UNIT_SIZE - (id % UNIT_SIZE) - 1)))
            }

            fun set(id: Int, bit: Boolean) {
                if (bit) {
                    units[id / UNIT_SIZE] = units[id / UNIT_SIZE] or (1u shl (id % UNIT_SIZE))
                } else {
                    units[id / UNIT_SIZE] = units[id / UNIT_SIZE] and (1u shl (id % UNIT_SIZE)).inv()
                }
            }

            fun isEmpty(): Boolean = units.isEmpty()
            fun numOnes(): Int = numOnes
            fun size(): Int = size

            fun append() {
                if ((size % UNIT_SIZE) == 0) {
                    units.append()
                }
                size++
            }

            fun build() {
                try {
                    ranks.reset(Array(units.size()) { 0u })
                } catch (e: OutOfMemoryError) {
                    DARTS_THROW("failed to build rank index: OutOfMemoryError")
                }

                numOnes = 0
                for (i in 0 until units.size()) {
                    ranks[i] = numOnes
                    numOnes += popCount(units[i])
                }
            }

            fun clear() {
                units.clear()
                ranks.clear()
            }

            companion object {
                const val UNIT_SIZE = 32

                private fun popCount(unit: IdType): Int {
                    var u = unit.toInt()
                    u = ((u and 0xAAAAAAAA.toInt()) shr 1) + (u and 0x55555555)
                    u = ((u and 0xCCCCCCCC.toInt()) shr 2) + (u and 0x33333333)
                    u = ((u shr 4) + u) and 0x0F0F0F0F
                    u += u shr 8
                    u += u shr 16
                    return u and 0xFF
                }
            }
        }
    }

    // <DoubleArrayImpl> is the interface of Darts-clone. Note that other
    // classes should not be accessed from outside.
    //
    // <DoubleArrayImpl> has 4 template arguments but only the 3rd one is used as
    // the type of values. Note that the given <T> is used only from outside, and
    // the internal value type is not changed from <Darts.Details.value_type>.
    // In build(), given values are casted from <T> to <Darts.Details.value_type>
    // by using static_cast. On the other hand, values are casted from
    // <Darts.Details.value_type> to <T> in searching dictionaries.
    class DoubleArrayImpl<T> {
        // Even if this <value_type> is changed, the internal value type is still
        // <Darts.Details.value_type>. Other types, such as 64-bit integer types
        // and floating-point number types, should not be used.
        typealias ValueType = T

        // A key is reprenseted by a sequence of <key_type>s. For example,
        // exactMatchSearch() takes a <const key_type *>.
        typealias KeyType = Details.CharType

        // In searching dictionaries, the values associated with the matched keys are
        // stored into or returned as <result_type>s.
        typealias ResultType = ValueType

        // <result_pair_type> enables applications to get the lengths of the matched
        // keys in addition to the values.
        data class ResultPairType(var value: ValueType, var length: Int)

        private var size = 0
        private var array: Array<Details.DoubleArrayUnit>? = null
        private var buf: Array<Details.DoubleArrayUnit>? = null

        // The constructor initializes member variables with 0 and NULLs.
        // The destructor frees memory allocated for units and then initializes
        // member variables with 0 and NULLs.
        fun clear() {
            size = 0
            array = null
            buf = null
        }

        // unit_size() returns the size of each unit. The size must be 4 bytes.
        fun unitSize(): Int = 4

        // size() returns the number of units. It can be 0 if set_array() is used.
        fun size(): Int = size

        // total_size() returns the number of bytes allocated to the array of units.
        // It can be 0 if set_array() is used.
        fun totalSize(): Int = unitSize() * size()

        // nonzero_size() exists for compatibility. It always returns the number of
        // units because it takes long time to count the number of non-zero units.
        fun nonzeroSize(): Int = size()

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
        // <Darts.Details.progress_func_type>.
        // The return value of build() is 0, and it indicates the success of the
        // operation. Otherwise, build() throws a <Darts.Exception>, which is a
        // derived class of <std::exception>.
        // build() uses another construction algorithm if `values' is not NULL. In
        // this case, Darts-clone uses a Directed Acyclic Word Graph (DAWG) instead
        // of a trie because a DAWG is likely to be more compact than a trie.
        fun build(
            numKeys: Int, keys: Array<String>, lengths: IntArray? = null, values: IntArray? = null,
            progressFunc: Details.ProgressFuncType? = null
        ): Int {
            val keyset = Keyset(numKeys, keys, lengths, values)
            val builder = Details.DoubleArrayBuilder(progressFunc)
            builder.build(keyset)
            val size = builder.size
            val buf = builder.buf

            clear()
            this.size = size
            this.array = buf
            this.buf = buf

            progressFunc?.invoke(numKeys + 1, numKeys + 1)
            return 0
        }

        // exactMatchSearch() tests whether the given key exists or not, and
        // if it exists, its value and length are set to `result'. Otherwise, the
        // value and the length of `result' are set to -1 and 0 respectively.
        // Note that if `length' is 0, `key' is handled as a zero-terminated string.
        // `node_pos' specifies the start position of matching. This argument enables
        // the combination of exactMatchSearch() and traverse(). For example, if you
        // want to test "xyzA", "xyzBC", and "xyzDE", you can use traverse() to get
        // the node position corresponding to "xyz" and then you can use
        // exactMatchSearch() to test "A", "BC", and "DE" from that position.
        // Note that the length of `result' indicates the length from the `node_pos'.
        // In the above example, the lengths are { 1, 2, 2 }, not { 4, 5, 5 }.
        inline fun <U> exactMatchSearch(key: String, result: U, length: Int = 0, nodePos: Int = 0) {
            result = exactMatchSearch(key, length, nodePos)
        }

        inline fun <U> exactMatchSearch(key: String, length: Int = 0, nodePos: Int = 0): U {
            var result: U = U()
            setResult(result, -1, 0)

            var unit = array!![nodePos]
            var pos = nodePos
            var len = length
            if (length != 0) {
                for (i in 0 until length) {
                    pos = pos xor unit.offset() xor key[i].toInt()
                    unit = array!![pos]
                    if (unit.label() != key[i].toInt().toUInt()) {
                        return result
                    }
                }
            } else {
                for (ch in key) {
                    pos = pos xor unit.offset() xor ch.toInt()
                    unit = array!![pos]
                    if (unit.label() != ch.toInt().toUInt()) {
                        return result
                    }
                    len++
                }
            }

            if (!unit.hasLeaf()) {
                return result
            }
            unit = array!![pos xor unit.offset()]
            setResult(result, unit.value(), len)
            return result
        }

        // commonPrefixSearch() searches for keys which match a prefix of the given
        // string. If `length' is 0, `key' is handled as a zero-terminated string.
        // The values and the lengths of at most `max_num_results' matched keys are
        // stored in `results'. commonPrefixSearch() returns the number of matched
        // keys. Note that the return value can be larger than `max_num_results' if
        // there are more than `max_num_results' matches. If you want to get all the
        // results, allocate more spaces and call commonPrefixSearch() again.
        // `node_pos' works as well as in exactMatchSearch().
        inline fun <U> commonPrefixSearch(
            key: String, results: Array<U>, maxNumResults: Int, length: Int = 0,
            nodePos: Int = 0
        ): Int {
            var numResults = 0

            var unit = array!![nodePos]
            var pos = nodePos xor unit.offset()
            var len = length
            if (length != 0) {
                for (i in 0 until length) {
                    pos = pos xor key[i].toInt()
                    unit = array!![pos]
                    if (unit.label() != key[i].toInt().toUInt()) {
                        return numResults
                    }

                    pos = pos xor unit.offset()
                    if (unit.hasLeaf()) {
                        if (numResults < maxNumResults) {
                            setResult(results[numResults], unit.value(), i + 1)
                        }
                        numResults++
                    }
                }
            } else {
                for (ch in key) {
                    pos = pos xor ch.toInt()
                    unit = array!![pos]
                    if (unit.label() != ch.toInt().toUInt()) {
                        return numResults
                    }

                    pos = pos xor unit.offset()
                    if (unit.hasLeaf()) {
                        if (numResults < maxNumResults) {
                            setResult(results[numResults], unit.value(), len + 1)
                        }
                        numResults++
                    }
                    len++
                }
            }

            return numResults
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
        inline fun traverse(key: String, nodePos: Int, keyPos: Int, length: Int = 0): Int {
            var id = nodePos
            var unit = array!![id]
            var pos = nodePos
            var keyPosition = keyPos

            if (length != 0) {
                for (i in keyPosition until length) {
                    pos = pos xor unit.offset() xor key[i].toInt()
                    unit = array!![pos]
                    if (unit.label() != key[i].toInt().toUInt()) {
                        return -2
                    }
                    id = pos
                    keyPosition++
                }
            } else {
                for (ch in key) {
                    pos = pos xor unit.offset() xor ch.toInt()
                    unit = array!![pos]
                    if (unit.label() != ch.toInt().toUInt()) {
                        return -2
                    }
                    id = pos
                    keyPosition++
                }
            }

            if (!unit.hasLeaf()) {
                return -1
            }
            unit = array!![id xor unit.offset()]
            return unit.value()
        }

        private fun <U> setResult(result: U, value: Int, length: Int) {
            when (result) {
                is Int -> result = value as U
                is ResultPairType -> result.apply {
                    this.value = value as ValueType
                    this.length = length
                }
                else -> throw IllegalArgumentException("Invalid result type")
            }
        }
    }

    // <DoubleArray> is the typical instance of <DoubleArrayImpl>. It uses <int>
    // as the type of values and it is suitable for most cases.
    typealias DoubleArray = DoubleArrayImpl<Int>

    // The interface section ends here. For using Darts-clone, there is no need
    // to read the remaining section, which gives the implementation of
    // Darts-clone.
}
