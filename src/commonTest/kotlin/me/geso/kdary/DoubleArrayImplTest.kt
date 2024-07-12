package me.geso.kdary

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

class DoubleArrayImplTest {
    private val random = Random(seed = 0)
    private val validKeys = generateValidKeys(NUM_VALID_KEYS, random)
    private val invalidKeys = generateInvalidKeys(NUM_INVALID_KEYS, validKeys, random)
    private val testData = buildData()
    private val keys: List<ByteArray> = testData.keys
    private val values: List<ValueType> = testData.values

    private fun buildData(): TestData {
        val keys: MutableList<ByteArray> = mutableListOf()
        val values: MutableList<ValueType> = mutableListOf()

        for ((keyId, key) in validKeys.sortedBy { String(it) }.withIndex()) {
            keys.add(key)
            values.add(keyId)
        }
        keys.forEachIndexed { index, key ->
            if (index <= 3 || index >= NUM_VALID_KEYS - 3) {
                println("index: $index, key: ${String(keys[index])}, value: ${values[index]}")
            }
        }
        assertEquals(0, values[0])

        // validKeys と invalidKeys の重複を確認する。
        val validKeyStrings = validKeys.map { String(it) }.toSet()
        val invalidKeyStrings = invalidKeys.map { String(it) }.toSet()
        val intersection = validKeyStrings.intersect(invalidKeyStrings)
        assertEquals(0, intersection.size)
        return TestData(keys, values)
    }

    data class TestData(
        val keys: List<ByteArray>,
        val values: List<ValueType>,
    )

    private fun generateValidKeys(
        numKeys: Int,
        random: Random,
    ): Set<ByteArray> {
        /*
void generate_valid_keys(std::size_t num_keys,
    std::set<std::string> *valid_keys) {
  std::vector<char> key;
  while (valid_keys->size() < num_keys) {
    key.resize(1 + (std::rand() % 8));
    for (std::size_t i = 0; i < key.size(); ++i) {
      key[i] = 'A' + (std::rand() % 26);
    }
    valid_keys->insert(std::string(&key[0], key.size()));
  }
         */
        val keys = mutableSetOf<String>()
        while (keys.size < numKeys) {
            val key = ByteArray(1 + (0..7).random())
            for (i in key.indices) {
                key[i] = ('A'.code + (0..25).random(random)).toByte()
            }
            keys.add(String(key))
        }
        return keys.map { it.toByteArray() }.toSet()
    }

    /*
void generate_invalid_keys(std::size_t num_keys,
    const std::set<std::string> &valid_keys,
    std::set<std::string> *invalid_keys) {
  std::vector<char> key;
  while (invalid_keys->size() < num_keys) {
    key.resize(1 + (std::rand() % 8));
    for (std::size_t i = 0; i < key.size(); ++i) {
      key[i] = 'A' + (std::rand() % 26);
    }
    std::string generated_key(&key[0], key.size());
    if (valid_keys.find(generated_key) == valid_keys.end())
      invalid_keys->insert(std::string(&key[0], key.size()));
  }
}
     */
    private fun generateInvalidKeys(
        numInvalidKeys: Int,
        validKeys: Set<ByteArray>,
        random: Random,
    ): Set<ByteArray> {
        val keys = mutableSetOf<String>()
        val validKeyStrings = validKeys.map { String(it) }.toSet()
        while (keys.size < numInvalidKeys) {
            val key = ByteArray(1 + (0..7).random(random))
            for (i in key.indices) {
                key[i] = ('A'.code + (0..25).random(random)).toByte()
            }
            if (!validKeyStrings.contains(String(key))) {
                keys.add(String(key))
            }
        }
        return keys.map { it.toByteArray() }.toSet()
    }

    @Test
    fun `build() with keys`() {
        val dic = DoubleArray.build<Any>(keys.toTypedArray())
        testDic(dic, keys, values, invalidKeys)
    }

    @Test
    fun `build() with keys, lengths`() {
        val dic = DoubleArray.build<Any>(keys.toTypedArray())
        testDic(dic, keys, values, invalidKeys)
    }

    @Test
    fun `build() with keys, lengths, values`() {
        val dic = DoubleArray.build(keys.toTypedArray(), values.toTypedArray())
        testDic(dic, keys, values, invalidKeys)
    }

    @Test
    fun `build() with keys, lengths and random values`() {
        /*
  for (std::size_t i = 0; i < values.size(); ++i) {
    values[i] = std::rand() % 10;
  }
         */
        val newValues = values.map { (0..9).random(random) }

        val dic = DoubleArray.build(keys.toTypedArray(), newValues.toTypedArray())
        testDic(dic, keys, newValues, invalidKeys)
    }

    @Test
    fun `save() and open()`() {
        val dic = DoubleArray.build(keys.toTypedArray(), values.toTypedArray())
        dic.save("test-darts.dic")

        val dicCopy = DoubleArray.open("test-darts.dic")

        assertEquals(dic.array()?.size, dicCopy.array()?.size)
        println(dic.array()?.size)
        dic.array()?.forEachIndexed { index, doubleArrayUnit ->
            assertEquals(dicCopy.array()?.get(index), doubleArrayUnit, "index=$index")
        }
        testDic(dicCopy, keys, values, invalidKeys)
    }

    /*
template <typename T>
void test_dic(const T &dic, const std::vector<const char *> &keys,
const std::vector<std::size_t> &lengths,
const std::vector<typename T::value_type> &values,
const std::set<std::string> &invalid_keys) {
typename T::value_type value;
typename T::result_pair_type result;

for (std::size_t i = 0; i < keys.size(); ++i) {
dic.exactMatchSearch(keys[i], value);
assert(value == values[i]);

dic.exactMatchSearch(keys[i], result);
assert(result.value == values[i]);
assert(result.length == lengths[i]);

dic.exactMatchSearch(keys[i], value, lengths[i]);
assert(value == values[i]);

dic.exactMatchSearch(keys[i], result, lengths[i]);
assert(result.value == values[i]);
assert(result.length == lengths[i]);
}

for (std::set<std::string>::const_iterator it = invalid_keys.begin();
it != invalid_keys.end(); ++it) {
dic.exactMatchSearch(it->c_str(), value);
assert(value == -1);

dic.exactMatchSearch(it->c_str(), result);
assert(result.value == -1);

dic.exactMatchSearch(it->c_str(), value, it->length());
assert(value == -1);

dic.exactMatchSearch(it->c_str(), result, it->length());
assert(result.value == -1);
}

std::cerr << "ok" << std::endl;
}
*/
    private fun testDic(
        dic: DoubleArray,
        keys: List<ByteArray>,
        values: List<ValueType>,
        invalidKeys: Set<ByteArray>,
    ) {
        for (i in keys.indices) {
            val result = dic.exactMatchSearch(keys[i])
//            debug("result=$result expected=${values[i]}")
            assert(result.value == values[i])
        }

        invalidKeys.forEach { invalidKey ->
            val result = dic.exactMatchSearch(invalidKey)
//            debug("result=$result invalidKey=$invalidKey")
            assert(result.value == -1)
        }
    }

    @Test
    fun testTraverse() {
/*
template <typename T>
void test_traverse(const T &dic,
    const std::vector<const char *> &keys,
    const std::vector<std::size_t> &lengths,
    const std::vector<typename T::value_type> &values,
    const std::set<std::string> &invalid_keys) {
  for (std::size_t i = 0; i < keys.size(); ++i) {
    const char *key = keys[i];
    std::size_t id = 0;
    std::size_t key_pos = 0;
    typename T::value_type result = 0;
    for (std::size_t j = 0; j < lengths[i]; ++j) {
      result = dic.traverse(key, id, key_pos, j + 1);
      assert(result != -2);
    }
    assert(result == values[i]);
  }

  for (std::set<std::string>::const_iterator it = invalid_keys.begin();
      it != invalid_keys.end(); ++it) {
    const char *key = it->c_str();
    std::size_t id = 0;
    std::size_t key_pos = 0;
    typename T::value_type result = 0;
    for (std::size_t i = 0; i < it->length(); ++i) {
      result = dic.traverse(key, id, key_pos, i + 1);
      if (result == -2) {
        break;
      }
    }
    assert(result < 0);
  }

  std::cerr << "ok" << std::endl;
}
 */
        val dic = DoubleArray.build(keys.toTypedArray(), values.toTypedArray())

        for (i in keys.indices) {
            val key = keys[i]
            var id = 0.toSizeType()
            var keyPos = 0.toSizeType()
            var result = 0
            for (j in 0uL until keys[i].size.toSizeType()) {
                val r = dic.traverse(key, id, keyPos, j + 1u)
                assert(r.status != -2)
                result = r.status
            }
            assert(result == values[i])
        }

        for (invalidKey in invalidKeys) {
            var id = 0.toSizeType()
            var keyPos = 0.toSizeType()
            var result = 0
            for (i in 0uL until invalidKey.size.toSizeType()) {
                val r = dic.traverse(invalidKey, id, keyPos, i + 1u)
                result = r.status
                if (result == -2) {
                    break
                }
            }
            assert(result < 0)
        }
    }

    @Test
    fun simple() {
        /*
  Darts::DoubleArray da;
  da.build(1, (const char *[]){"abc"}, (std::size_t[]) {3}, (int[]) {1});
  std::cout << "----------" << std::endl;

  int v = da.exactMatchSearch<int>("abc");
  std::cout << v << std::endl;
         */
        val dic = DoubleArray.build(arrayOf("abc".toByteArray()), arrayOf(4))
        println("----------")
        val v = dic.exactMatchSearch("abc".toByteArray())
        println(v)
        assertEquals(4, v.value)
    }

    @Test
    fun testCommonPrefixSearchSimple() {
        val dic =
            DoubleArray.build(
                arrayOf(
                    "京都".toByteArray(),
                    "東".toByteArray(),
                    "東京都".toByteArray(),
                ),
                arrayOf(5963, 4649, 7676),
            )
        println("----------")
        val result = dic.commonPrefixSearch("東京都庁".toByteArray())
        println(result)
        assertEquals(2, result.size)
        assertEquals(4649, result[0].value)
        assertEquals(7676, result[1].value)
    }

    @Test
    fun testCommonPrefixSearch() {
        /*
template <typename T>
void test_common_prefix_search(const T &dic,
    const std::vector<const char *> &keys,
    const std::vector<std::size_t> &lengths,
    const std::vector<typename T::value_type> &values,
    const std::set<std::string> &invalid_keys) {
  static const std::size_t MAX_NUM_RESULTS = 16;
  typename T::result_pair_type results[MAX_NUM_RESULTS];
  typename T::result_pair_type results_with_length[MAX_NUM_RESULTS];

  for (std::size_t i = 0; i < keys.size(); ++i) {
    std::size_t num_results = dic.commonPrefixSearch(
        keys[i], results, MAX_NUM_RESULTS);

    assert(num_results >= 1);
    assert(num_results < 10);

    assert(results[num_results - 1].value == values[i]);
    assert(results[num_results - 1].length == lengths[i]);

    std::size_t num_results_with_length = dic.commonPrefixSearch(
        keys[i], results_with_length, MAX_NUM_RESULTS, lengths[i]);

    assert(num_results == num_results_with_length);
    for (std::size_t j = 0; j < num_results; ++j) {
      assert(results[j].value == results_with_length[j].value);
      assert(results[j].length == results_with_length[j].length);
    }
  }


  std::cerr << "ok" << std::endl;
}
         */
        val dic = DoubleArray.build(keys.toTypedArray(), values.toTypedArray())
        for (i in keys.indices) {
            val key: ByteArray = keys[i]
            val results = dic.commonPrefixSearch(key)
//            println("key=${String(key.toByteArray())} results=$results")
            assert(results.size >= 1)
            assert(results.size < 10)
            assert(results[results.size - 1].value == values[i])
            assert(results[results.size - 1].length.toInt() == key.size)
        }

/*
      for (std::set<std::string>::const_iterator it = invalid_keys.begin();
      it != invalid_keys.end(); ++it) {
    std::size_t num_results = dic.commonPrefixSearch(
        it->c_str(), results, MAX_NUM_RESULTS);

    assert(num_results < 10);

    if (num_results > 0) {
      assert(results[num_results - 1].value != -1);
      assert(results[num_results - 1].length < it->length());
    }

    std::size_t num_results_with_length = dic.commonPrefixSearch(
        it->c_str(), results_with_length, MAX_NUM_RESULTS, it->length());

    assert(num_results == num_results_with_length);
    for (std::size_t j = 0; j < num_results; ++j) {
      assert(results[j].value == results_with_length[j].value);
      assert(results[j].length == results_with_length[j].length);
    }
  }
}
 */
        for (invalidKey in invalidKeys) {
            val results = dic.commonPrefixSearch(invalidKey)
            assert(results.size < 10)

            if (results.isNotEmpty()) {
                assert(results[results.size - 1].value != -1)
                assert(results[results.size - 1].length.toInt() < invalidKey.size)
            }
        }
    }

    companion object {
        const val NUM_VALID_KEYS = 1 shl 16
        const val NUM_INVALID_KEYS = 1 shl 17
    }
}
