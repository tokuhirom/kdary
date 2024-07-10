@file:OptIn(ExperimentalUnsignedTypes::class)

package me.geso.kdary

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
class DoubleArrayImpl<T : Number> {
    private var size: SizeType = 0u

    //   typedef Details::DoubleArrayUnit unit_type;
    // const unit_type *array_;
    private var array: Array<DoubleArrayUnit>? = null

    // unit_type *buf_;
    private var buf: Array<DoubleArrayUnit>? = null

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

    // setArray() はクリア() を呼び出して古い配列に割り当てられたメモリを解放し、その後新しい配列を設定します。
    // この関数はメモリマップド配列を設定するのに便利です。
    // setArray() で設定された配列はクリア() や <DoubleArrayImpl> のデストラクタでは解放されません。
    // setArray() は新しい配列のサイズも設定できますが、そのサイズは検索メソッドでは使用されません。
    // そのため、2番目の引数が0または省略された場合でも問題ありません。
    // その場合、size() と totalSize() は0を返します。
    fun setArray(
        ptr: Array<DoubleArrayUnit>?,
        size: SizeType = 0u,
    ) {
        clear()
        array = ptr
        this.size = size
    }

    // array() はユニットの配列を返します
    fun array(): Array<DoubleArrayUnit>? = array

    // clear メソッドは C++ だとメモリーの解放を行うが、Kotlin だと GC がやってくれるからあまり意味はないかも。
    fun clear() {
        size = 0u
        array = null
        buf = null
    }

    // unit_size() returns the size of each unit. The size must be 4 bytes.
    fun unitSize(): SizeType = 4u

    // size() returns the number of units. It can be 0 if set_array() is used.
    fun size(): SizeType = size

    // total_size() returns the total size of the array in bytes.
    fun totalSize(): SizeType = unitSize() * size()

    // empty() returns true if the array is empty.
    fun nonzeroSize(): SizeType = size()

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
    fun build(
        numKeys: SizeType,
        // XXX keys は sorted であること。
        keys: Array<UByteArray>,
        lengths: Array<SizeType>? = null,
        values: Array<T>? = null,
        progressFunc: ProgressFuncType? = null,
    ): Int {
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
        val keyset = Keyset(numKeys, keys, lengths, values)

        val builder = DoubleArrayBuilder(progressFunc)
        builder.build(keyset)

//        std::size_t size = 0;
//        unit_type *buf = NULL;
//        builder.copy(&size, &buf);
        // C++ ではポインタの参照を渡しているが、kotlin では返り値とするのが自然。
        val buf = builder.copy()

        clear()

        this.size = buf.size.toSizeType()
        this.array = buf
        this.buf = buf

        progressFunc?.invoke(numKeys + 1u, numKeys + 1u)

        return 0
    }

    // open() reads an array of units from the specified file. And if it goes
    // well, the old array will be freed and replaced with the new array read
    // from the file. `offset' specifies the number of bytes to be skipped before
    // reading an array. `size' specifies the number of bytes to be read from the
    // file. If the `size' is 0, the whole file will be read.
    // open() returns 0 iff the operation succeeds. Otherwise, it returns a
    // non-zero value or throws a <Darts::Exception>. The exception is thrown
    // when and only when a memory allocation fails.
//    int open(const char *file_name, const char *mode = "rb",
//    std::size_t offset = 0, std::size_t size = 0);
    fun open(
        fileName: String,
        // mode オプションは okio においては不要。
//        mode: String = "rb",
        offset: SizeType = 0u,
        size: SizeType = 0u,
    ): Int {
        // open() は指定されたファイルからユニットの配列を読み込みます。問題がなければ、古い配列は解放され、
        // ファイルから読み取られた新しい配列に置き換えられます。`offset` は配列を読み取る前にスキップするバイト数を指定します。
        // `size` はファイルから読み取るバイト数を指定します。`size` が0の場合、ファイル全体が読み取られます。

        val file = File(fileName)
        if (!file.exists()) {
            return -1
        }

        file.source().buffer().use { source ->
            var actualSize = size
            if (actualSize == 0uL) {
                actualSize = file.length().toULong() - offset
            }

            val unitSize = unitSize()
            val numUnits = actualSize / unitSize
            if (numUnits < 256uL || numUnits % 256u != 0uL) {
                return -1
            }

            source.skip(offset.toLong())

            val units = Array(256) { DoubleArrayUnit() }
            for (i in units.indices) {
                units[i] = DoubleArrayUnit(readUIntLe(source))
            }

            if (units[0].label().toInt().toChar() != '\u0000' ||
                units[0].hasLeaf() ||
                units[0].offset() == 0u ||
                units[0].offset() >= 512u
            ) {
                return -1
            }

            for (i in 1 until 256) {
                if (units[i].label() <= 0xFF.toUInt() && units[i].offset() >= numUnits.toUInt()) {
                    return -1
                }
            }

            val buf: Array<DoubleArrayUnit> =
                try {
                    Array(numUnits.toInt()) { DoubleArrayUnit() }
                } catch (e: OutOfMemoryError) {
                    throw DartsException("failed to open double-array: std::bad_alloc")
                }

            for (i in units.indices) {
                buf[i] = units[i]
            }

            if (numUnits > 256u) {
                // 残りのユニットを読み込む
                for (i in 256 until numUnits.toInt()) {
                    buf[i] =
                        DoubleArrayUnit().apply {
                            // 必要に応じて各フィールドを読み込む
                        }
                }
            }

            this.size = numUnits
            this.array = buf
            this.buf = buf
        }

        return 0
    }

    // save() writes the array of units into the specified file. `offset'
    // specifies the number of bytes to be skipped before writing the array.
    // open() returns 0 iff the operation succeeds. Otherwise, it returns a
    // non-zero value.
//    int save(const char *file_name, const char *mode = "wb",
//    std::size_t offset = 0) const;
    fun save(
        fileName: String,
//        mode: String = "wb",
        offset: SizeType,
    ): Int {
        if (size == 0uL) {
            return -1
        }

        val file = File(fileName)
        file.sink().buffer().use { sink ->
            sink.write(ByteArray(offset.toInt())) // オフセット分の空バイトを書き込み
            array?.forEach { unit ->
                writeUIntLe(sink, unit.unit)
            }
        }

        return 0
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
        key: UByteArray,
        nodePos: SizeType = 0u,
    ): ResultPairType<Int> = exactMatchSearchInternal(key, nodePos)

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
        var unit = array?.get(nodePosParam.toInt()) ?: return ResultPairType(-1, 0u)
        var nodePos = nodePosParam
        val length = key.size.toSizeType()
        debug("length=$length")
        for (i in 0uL until length) {
            debug("i=$i, length=$length")
            // TODO xor 動いてる?
            debug("nodePos: $nodePos, unit.offset(): ${unit.offset()}")
            nodePos = nodePos xor ((unit.offset() xor key[i.toInt()].toUInt()).toULong())
            debug("xor result=$nodePos")
            unit = array?.get(nodePos.toInt()) ?: return ResultPairType(-1, 0u)
            debug("unit.label=${unit.label()}")
            if (unit.label() != key[i.toInt()].toUInt()) {
                return ResultPairType(-1, 0u)
            }
        }

        debug("Checking leaf: ${unit.hasLeaf()}")
        if (!unit.hasLeaf()) {
            return ResultPairType(-1, 0u)
        }
        unit = array?.get(nodePos.toInt() xor unit.offset().toInt()) ?: return ResultPairType(-1, 0u)
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

        var unit: DoubleArrayUnit = array?.get(nodePos.toInt()) ?: return numResults
        nodePos = nodePos xor unit.offset().toSizeType()

        if (lengthParam != 0uL) {
            for (i in 0uL until length) {
                // TODO ここのビット操作がめっちゃ怪しい
                nodePos = nodePos xor key[i.toInt()].toUByte().toInt().toSizeType()
                unit = array?.get(nodePos.toInt()) ?: return numResults
                if (unit.label() != (key[i.toInt()].toUInt() and 0xFFU)) {
                    return numResults
                }

                nodePos = nodePos xor unit.offset().toSizeType()
                if (unit.hasLeaf()) {
                    if (numResults < maxNumResults) {
                        val v = array?.get(nodePos.toInt())?.value() ?: throw IllegalStateException()
                        results[numResults.toInt()].set(v, (i + 1u))
                    }
                    numResults++
                }
            }
        } else {
            while (key[length.toInt()] != 0.toByte()) {
                // xor が怪しい
                nodePos = nodePos xor key[length.toInt()].toUByte().toInt().toSizeType()
                unit = array?.get(nodePos.toInt()) ?: return numResults
                if (unit.label() != key[length.toInt()].toUByte().toIdType()) {
                    return numResults
                }

                nodePos = nodePos xor unit.offset().toSizeType()
                if (unit.hasLeaf()) {
                    if (numResults < maxNumResults) {
                        val v = array?.get(nodePos.toInt())?.value() ?: throw IllegalStateException()
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
        key: UByteArray,
        nodePos: SizeType,
        keyPos: SizeType,
        length: SizeType = 0u,
    ): TraverseResult = traverseInternal(key, nodePos, keyPos, length)

    private fun traverseInternal(
        key: UByteArray,
        nodePosParam: SizeType,
        keyPosParam: SizeType,
        length: SizeType = 0u,
    ): TraverseResult {
        var id: IdType = nodePosParam.toIdType()

        var unit = array?.get(id.toInt()) ?: return TraverseResult(-2, nodePosParam, keyPosParam)

        // 変更するために、var にアサインする
        var nodePos = nodePosParam
        var keyPos = keyPosParam

        if (length != 0uL) {
            while (keyPos < length) {
                id = id xor (unit.offset() xor key[keyPos.toInt()].toUInt())
                unit = array?.get(id.toInt()) ?: return TraverseResult(-2, nodePos, keyPos)
                if (unit.label() != key[keyPos.toInt()].toUInt()) {
                    return TraverseResult(-2, nodePos, keyPos)
                }
                nodePos = id.toSizeType()

                keyPos++
            }
        } else {
            while (key[keyPos.toInt()].toInt() != 0) {
                id = id xor (unit.offset() xor key[keyPos.toInt()].toUInt())
                unit = array?.get(id.toInt()) ?: return TraverseResult(-2, nodePos, keyPos)
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
            unit = array?.get((id xor unit.offset()).toInt()) ?: return TraverseResult(-2, nodePos, keyPos)
            TraverseResult(unit.value(), nodePos, keyPos)
        }
    }

    data class TraverseResult(
        val status: Int,
        val nodePos: SizeType? = null,
        val keyPos: SizeType? = null,
    )

    // TODO: `as T` が多すぎる。 kotlin ならば、もう少しうまくやれるんじゃないか?
    // 例外を投げた方が良いかもしれない。
}
