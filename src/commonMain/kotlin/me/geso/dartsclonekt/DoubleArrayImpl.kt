package me.geso.dartsclonekt

// C++ 実装での value_type, result_type は T になります。
// key_type は Byte です。
class DoubleArrayImpl<T> {
    private var size: SizeType = 0u

    //   typedef Details::DoubleArrayUnit unit_type;
    // const unit_type *array_;
    private var array: Array<DoubleArrayUnit>? = null

    // unit_type *buf_;
    private val buf: Array<DoubleArrayUnit>? = null

    // <ResultPairType> は一致するキーの長さに加えて値を取得するためにアプリケーションが使用できるようにします。
    data class ResultPairType<T>(
        var value: T,
        var length: SizeType,
    )

    // <DoubleArrayImpl> has 2 kinds of set_result()s. The 1st set_result() is to
    // set a value to a <value_type>. The 2nd set_result() is to set a value and
    // a length to a <result_pair_type>. By using set_result()s, search methods
    // can return the 2 kinds of results in the same way.
    // Why the set_result()s are non-static? It is for compatibility.
    //
    // The 1st set_result() takes a length as the 3rd argument but it is not
    // used. If a compiler does a good job, codes for getting the length may be
    // removed.
//    void set_result(value_type *result, value_type value, std::size_t) const {
//        *result = value;
//    }
    // The 2nd set_result() uses both `value' and `length'.
//    void set_result(result_pair_type *result,
//    value_type value, std::size_t length) const {
//        result->value = value;
//        result->length = length;
//    }

    // TODO: この 2つの set_result() は Kotlin でどう書き換えるのか？
    // おそらく、拡張関数として実装するのが素直で、インスタンスメソッドにはならない。

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
        array = ptr
        this.size = size
    }

    // array() はユニットの配列を返します
    fun array(): Array<DoubleArrayUnit>? = array

    // clear メソッドは C++ だとメモリーの解放を行うが、Kotlin だと GC がやってくれるので不要。
//    void clear() {

    // unit_size() returns the size of each unit. The size must be 4 bytes.
//    std::size_t unit_size() const {
//        return sizeof(unit_type);
//    }
    fun unitSize(): SizeType = 4u

    // size() returns the number of units. It can be 0 if set_array() is used.
//    std::size_t size() const {
//        return size_;
//    }
    fun size(): SizeType = size

    // total_size() returns the total size of the array in bytes.
//    std::size_t total_size() const {
//        return unit_size() * size();
//    }
    fun totalSize(): SizeType = unitSize() * size()

    // empty() returns true if the array is empty.
//    std::size_t nonzero_size() const {
//        return size();
//    }
    fun nonzeroSize(): SizeType = size

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
        keys: Array<KeyType>,
        lengths: Array<SizeType>? = null,
        values: Array<T>? = null,
        progressFunc: ProgressFuncType? = null,
    ): Int {
        TODO()
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
        mode: String = "rb",
        offset: SizeType = 0u,
        size: SizeType = 0u,
    ): Int {
        TODO()
    }

    // save() writes the array of units into the specified file. `offset'
    // specifies the number of bytes to be skipped before writing the array.
    // open() returns 0 iff the operation succeeds. Otherwise, it returns a
    // non-zero value.
//    int save(const char *file_name, const char *mode = "wb",
//    std::size_t offset = 0) const;
    fun save(
        fileName: String,
        mode: String = "wb",
        offset: SizeType,
    ) {
        TODO()
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
    fun exactMatchSearch(
        key: Array<KeyType>,
        result: ResultPairType<T>,
        length: SizeType = 0u,
        nodePos: SizeType = 0u,
    ) {
        TODO()
    }

    // The 2nd exactMatchSearch() returns a result instead of updating the 2nd
    // argument. So, the following exactMatchSearch() has only 3 arguments.
//    template <class U>
//    inline U exactMatchSearch(const key_type *key, std::size_t length = 0,
//    std::size_t node_pos = 0) const;
    fun exactMatchSearch(
        key: Array<KeyType>,
        length: SizeType = 0u,
        nodePos: SizeType = 0u,
    ): ResultPairType<T> {
        TODO()
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
        results: Array<ResultPairType<T>>,
        maxNumResults: SizeType,
        length: SizeType = 0u,
        nodePos: SizeType = 0u,
    ): SizeType {
        TODO()
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
        key: Array<KeyType>,
        nodePos: SizeType,
        keyPos: SizeType,
        length: SizeType = 0u,
    ): T {
        TODO()
    }
}

