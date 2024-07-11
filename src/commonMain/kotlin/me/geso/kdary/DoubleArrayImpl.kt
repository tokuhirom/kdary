@file:OptIn(ExperimentalUnsignedTypes::class)

package me.geso.kdary

import okio.IOException
import okio.buffer
import okio.sink
import okio.source
import java.io.File

private fun debug(message: String) {
//    println("[D] $message")
}

private fun readUIntLe(source: okio.BufferedSource): UInt {
    val byte1 = source.readByte().toUInt() and 0xFFU
    val byte2 = source.readByte().toUInt() and 0xFFU
    val byte3 = source.readByte().toUInt() and 0xFFU
    val byte4 = source.readByte().toUInt() and 0xFFU
    return (byte1 or (byte2 shl 8) or (byte3 shl 16) or (byte4 shl 24))
}

// ユニットのデータをリトルエンディアンで書き込む関数
private fun writeUIntLe(
    sink: okio.BufferedSink,
    value: UInt,
) {
    sink.writeByte((value and 0xFFU).toInt())
    sink.writeByte(((value shr 8) and 0xFFU).toInt())
    sink.writeByte(((value shr 16) and 0xFFU).toInt())
    sink.writeByte(((value shr 24) and 0xFFU).toInt())
}

// KeyType は  drts-clone では Char だが、Kotlin の Char は 16bit なので Byte にしている。
// Byte は signed 8bit。
private typealias KeyType = Byte

// C++ 実装での value_type, result_type は T になります。
// key_type は Byte です。
class DoubleArrayImpl<T : Number>(
    //   typedef Details::DoubleArrayUnit unit_type;
    // const unit_type *array_;
    private var array: Array<DoubleArrayUnit>,
) {
    private var size: SizeType = array.size.toSizeType()

    // <ResultPairType> は一致するキーの長さに加えて値を取得するためにアプリケーションが使用できるようにします。
    data class ResultPairType<T>(
        var value: T,
        var length: SizeType,
    ) {
        // set_result 相当
        fun set(
            value: T,
            length: SizeType,
        ) {
            @Suppress("UNCHECKED_CAST")
            this.value = value
            this.length = length
        }
    }

    // array() はユニットの配列を返します
    internal fun array(): Array<DoubleArrayUnit>? = array

    // size() returns the number of units. It can be 0 if set_array() is used.
    fun size(): SizeType = size

    // total_size() returns the total size of the array in bytes.
    fun totalSize(): SizeType = unitSize() * size()

    // empty() returns true if the array is empty.
    fun nonzeroSize(): SizeType = size()

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

    // The 1st exactMatchSearch() tests whether the given key exists or not, and
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
//    template <class U>
//    void exactMatchSearch(const key_type *key, U &result,
//    std::size_t length = 0, std::size_t node_pos = 0) const {
//        result = exactMatchSearch<U>(key, length, node_pos);
//    }
    // darts-clone と違って、length パラメータを消す。
    // length パラメータは、key の長さを指定するもので、0 の場合は key が null 終端文字列として扱われる。
    // しかし、kotlin では ByteArray が長さを持つので、length が必要ない。
    fun exactMatchSearch(
        key: ByteArray,
        nodePos: SizeType = 0u,
    ): ResultPairType<Int> = exactMatchSearchInternal(key.toUByteArray(), nodePos)

    private fun exactMatchSearchInternal(
        key: UByteArray,
        nodePosParam: SizeType = 0u,
    ): ResultPairType<Int> {
        /*
template <typename A, typename B, typename T, typename C>
template <typename U>
inline U DoubleArrayImpl<A, B, T, C>::exactMatchSearch(const key_type *key,
    std::size_t length, std::size_t node_pos) const {
  U result;
  set_result(&result, static_cast<value_type>(-1), 0);

  unit_type unit = array_[node_pos];
  if (length != 0) {
    for (std::size_t i = 0; i < length; ++i) {
      node_pos ^= unit.offset() ^ static_cast<uchar_type>(key[i]);
      unit = array_[node_pos];
      if (unit.label() != static_cast<uchar_type>(key[i])) {
        return result;
      }
    }
  } else {
    for ( ; key[length] != '\0'; ++length) {
      node_pos ^= unit.offset() ^ static_cast<uchar_type>(key[length]);
      unit = array_[node_pos];
      if (unit.label() != static_cast<uchar_type>(key[length])) {
        return result;
      }
    }
  }

  if (!unit.has_leaf()) {
    return result;
  }
  unit = array_[node_pos ^ unit.offset()];
  set_result(&result, static_cast<value_type>(unit.value()), length);
  return result;
}
         */
        var unit = array[nodePosParam.toInt()]
        var nodePos = nodePosParam
        val length = key.size.toSizeType()
        debug("length=$length")
        for (i in 0uL until length) {
            debug("i=$i, length=$length")
            // TODO xor 動いてる?
            debug("nodePos: $nodePos, unit.offset(): ${unit.offset()}")
            nodePos = nodePos xor ((unit.offset() xor key[i.toInt()].toUInt()).toULong())
            debug("xor result=$nodePos")
            unit = array[nodePos.toInt()] ?: return ResultPairType(-1, 0u)
            debug("unit.label=${unit.label()}")
            if (unit.label() != key[i.toInt()].toUInt()) {
                return ResultPairType(-1, 0u)
            }
        }

        debug("Checking leaf: ${unit.hasLeaf()}")
        if (!unit.hasLeaf()) {
            return ResultPairType(-1, 0u)
        }
        unit = array[nodePos.toInt() xor unit.offset().toInt()] ?: return ResultPairType(-1, 0u)
        return ResultPairType(unit.value(), length)
    }

    // commonPrefixSearch() searches for keys which match a prefix of the given
    // string. If `length' is 0, `key' is handled as a zero-terminated string.
    // The values and the lengths of at most `max_num_results' matched keys are
    // stored in `results'. commonPrefixSearch() returns the number of matched
    // keys. Note that the return value can be larger than `max_num_results' if
    // there are more than `max_num_results' matches. If you want to get all the
    // results, allocate more spaces and call commonPrefixSearch() again.
    // `node_pos' works as well as in exactMatchSearch().
//    template <class U>
//    inline std::size_t commonPrefixSearch(const key_type *key, U *results,
//    std::size_t max_num_results, std::size_t length = 0,
//    std::size_t node_pos = 0) const;
    fun commonPrefixSearch(
        key: Array<KeyType>,
        results: Array<ResultPairType<ValueType>>,
        maxNumResults: SizeType,
        length: SizeType = 0u,
        nodePos: SizeType = 0u,
    ): SizeType = commonPrefixSearchInternal(key, results, maxNumResults, length, nodePos)

    private fun commonPrefixSearchInternal(
        key: Array<KeyType>,
        results: Array<ResultPairType<ValueType>>,
        maxNumResults: SizeType,
        lengthParam: SizeType = 0u,
        nodePosParam: SizeType = 0u,
    ): SizeType {
        var numResults: SizeType = 0u

        var nodePos: SizeType = nodePosParam
        var length: SizeType = lengthParam

        var unit: DoubleArrayUnit = array[nodePos.toInt()]
        nodePos = nodePos xor unit.offset().toSizeType()

        if (lengthParam != 0uL) {
            for (i in 0uL until length) {
                // TODO ここのビット操作がめっちゃ怪しい
                nodePos = nodePos xor key[i.toInt()].toUByte().toInt().toSizeType()
                unit = array[nodePos.toInt()]
                if (unit.label() != (key[i.toInt()].toUInt() and 0xFFU)) {
                    return numResults
                }

                nodePos = nodePos xor unit.offset().toSizeType()
                if (unit.hasLeaf()) {
                    if (numResults < maxNumResults) {
                        val v = array[nodePos.toInt()].value()
                        results[numResults.toInt()].set(v, (i + 1u))
                    }
                    numResults++
                }
            }
        } else {
            while (key[length.toInt()] != 0.toByte()) {
                // xor が怪しい
                nodePos = nodePos xor key[length.toInt()].toUByte().toInt().toSizeType()
                unit = array[nodePos.toInt()]
                if (unit.label() != key[length.toInt()].toUByte().toIdType()) {
                    return numResults
                }

                nodePos = nodePos xor unit.offset().toSizeType()
                if (unit.hasLeaf()) {
                    if (numResults < maxNumResults) {
                        val v = array[nodePos.toInt()].value()
                        results[numResults.toInt()].set(v, (length + 1u))
                    }
                    numResults++
                }

                length++
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
//    inline value_type traverse(const key_type *key, std::size_t &node_pos,
//    std::size_t &key_pos, std::size_t length = 0) const;
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

        // 変更するために、var にアサインする
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
//    int build(std::size_t num_keys, const key_type * const *keys,
//    const std::size_t *lengths = NULL, const value_type *values = NULL,
//    Details::progress_func_type progress_func = NULL);
        fun <T> build(
            // XXX keys は sorted であること。
            keys: Array<UByteArray>,
            values: Array<T>? = null,
            progressFunc: ProgressFuncType? = null,
        ): DoubleArray {
            /*
    template <typename A, typename B, typename T, typename C>
    int DoubleArrayImpl<A, B, T, C>::build(std::size_t num_keys,
        const key_type * const *keys, const std::size_t *lengths,
        const value_type *values, Details::progress_func_type progress_func) {
      Details::Keyset<value_type> keyset(num_keys, keys, lengths, values);

      Details::DoubleArrayBuilder builder(progress_func);
      builder.build(keyset);

      std::size_t size = 0;
      unit_type *buf = NULL;
      builder.copy(&size, &buf);

      clear();

      size_ = size;
      array_ = buf;
      buf_ = buf;

      if (progress_func != NULL) {
        progress_func(num_keys + 1, num_keys + 1);
      }

      return 0;
    }
             */
            val keyset = Keyset(keys, values)

            val builder = DoubleArrayBuilder(progressFunc)
            builder.build(keyset)

            // C++ ではポインタの参照を渡しているが、kotlin では返り値とするのが自然。
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
