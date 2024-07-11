@file:OptIn(ExperimentalUnsignedTypes::class)

package me.geso.kdary

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

// static const std::size_t NUM_VALID_KEYS = 1 << 16;
// static const std::size_t NUM_INVALID_KEYS = 1 << 17;
//
// std::set<std::string> valid_keys;
// generate_valid_keys(NUM_VALID_KEYS, &valid_keys);
//
// std::set<std::string> invalid_keys;
// generate_invalid_keys(NUM_INVALID_KEYS, valid_keys, &invalid_keys);

private fun debug(message: String) {
    println("[D] $message")
}

class DoubleArrayImplTest {
    private val random = Random(seed = 0)
    private val validKeys = generateValidKeys(NUM_VALID_KEYS, random)
    private val invalidKeys = generateInvalidKeys(NUM_INVALID_KEYS, validKeys, random)
    private val testData = buildData()
    private val keys: List<UByteArray> = testData.keys
    private val lengths: List<SizeType> = testData.lengths
    private val values: List<ValueType> = testData.values

    init {
        /*
  std::size_t key_id = 0;
  for (std::set<std::string>::const_iterator it = valid_keys.begin();
      it != valid_keys.end(); ++it, ++key_id) {
    keys[key_id] = it->c_str();
    lengths[key_id] = it->length();
    values[key_id] = static_cast<typename T::value_type>(key_id);
  }
         */
    }

    private fun buildData(): TestData {
        val keys: MutableList<UByteArray> = mutableListOf()
        val lengths: MutableList<SizeType> = mutableListOf()
        val values: MutableList<ValueType> = mutableListOf()

        for ((keyId, key) in validKeys.sortedBy { String(it.toByteArray()) }.withIndex()) {
            keys.add(key)
            lengths.add(key.size.toSizeType())
            values.add(keyId)
        }
        keys.forEachIndexed { index, key ->
            if (index <= 3 || index >= NUM_VALID_KEYS - 3) {
                println("index: $index, key: ${String(keys[index].toByteArray())}, length: ${lengths[index]}, value: ${values[index]}")
            }
        }
        assertEquals(0, values[0])

        // validKeys と invalidKeys の重複を確認する。
        val validKeyStrings = validKeys.map { String(it.toByteArray()) }.toSet()
        val invalidKeyStrings = invalidKeys.map { String(it.toByteArray()) }.toSet()
        val intersection = validKeyStrings.intersect(invalidKeyStrings)
        assertEquals(0, intersection.size)
        return TestData(keys, lengths, values)
    }

    data class TestData(
        val keys: List<UByteArray>,
        val lengths: List<SizeType>,
        val values: List<ValueType>,
    )

    private fun generateValidKeys(
        numKeys: Int,
        random: Random,
    ): Set<UByteArray> {
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
            val key = UByteArray(1 + (0..7).random())
            for (i in key.indices) {
                key[i] = ('A'.code + (0..25).random(random)).toUByte()
            }
            keys.add(String(key.toByteArray()))
        }
        return keys.map { it.toUByteArray() }.toSet()
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
        validKeys: Set<UByteArray>,
        random: Random,
    ): Set<UByteArray> {
        val keys = mutableSetOf<String>()
        val validKeyStrings = validKeys.map { String(it.toByteArray()) }.toSet()
        while (keys.size < numInvalidKeys) {
            val key = UByteArray(1 + (0..7).random(random))
            for (i in key.indices) {
                key[i] = ('A'.code + (0..25).random(random)).toUByte()
            }
            if (!validKeyStrings.contains(String(key.toByteArray()))) {
                keys.add(String(key.toByteArray()))
            }
        }
        return keys.map { it.toUByteArray() }.toSet()
    }

    @Test
    fun `build() with keys`() {
        val dic = DoubleArray.build<Any>(keys.toTypedArray())
        testDic(dic, keys, lengths, values, invalidKeys)
    }

    @Test
    fun `build() with keys, lengths`() {
        val dic = DoubleArray.build<Any>(keys.toTypedArray(), lengths.toTypedArray())
        testDic(dic, keys, lengths, values, invalidKeys)
    }

    @Test
    fun `build() with keys, lengths, values`() {
        val dic = DoubleArray.build(keys.toTypedArray(), lengths.toTypedArray(), values.toTypedArray())
        testDic(dic, keys, lengths, values, invalidKeys)
    }

    // FAILING...
    @Test
    fun `build() with keys, lengths and random values`() {
        /*
  for (std::size_t i = 0; i < values.size(); ++i) {
    values[i] = std::rand() % 10;
  }
         */
        val newValues = values.map { (0..9).random(random) }

        val dic = DoubleArray.build(keys.toTypedArray(), lengths.toTypedArray(), newValues.toTypedArray())
        testDic(dic, keys, lengths, newValues, invalidKeys)
    }

    @Test
    fun `save() and open()`() {
        val dic = DoubleArray.build(keys.toTypedArray(), lengths.toTypedArray(), values.toTypedArray())
        assert(dic.save("test-darts.dic", 0uL) == 0)

        val dicCopy = DoubleArray.open("test-darts.dic")

        assertEquals(dic.array()?.size, dicCopy.array()?.size)
        println(dic.array()?.size)
        dic.array()?.forEachIndexed { index, doubleArrayUnit ->
            assertEquals(dicCopy.array()?.get(index), doubleArrayUnit, "index=$index")
        }
        testDic(dicCopy, keys, lengths, values, invalidKeys)
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
        keys: List<UByteArray>,
        lengths: List<SizeType>,
        values: List<ValueType>,
        invalidKeys: Set<UByteArray>,
    ) {
        for (i in keys.indices) {
            val result = dic.exactMatchSearch(keys[i].toByteArray())
//            debug("result=$result expected=${values[i]}")
            assert(result.value == values[i])
        }

        invalidKeys.forEach { invalidKey ->
            val result = dic.exactMatchSearch(invalidKey.toByteArray())
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
        val dic = DoubleArray.build(keys.toTypedArray(), lengths.toTypedArray(), values.toTypedArray())

        for (i in keys.indices) {
            val key = keys[i]
            var id = 0.toSizeType()
            var keyPos = 0.toSizeType()
            var result = 0
            for (j in 0uL until lengths[i]) {
                val r = dic.traverse(key.toByteArray(), id, keyPos, j + 1u)
                assert(r.status != -2)
                result = r.status
            }
            assert(result == values[i])
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
        val dic = DoubleArray.build(arrayOf("abc".toUByteArray()), arrayOf(3.toSizeType()), arrayOf(4))
        println("----------")
        val v = dic.exactMatchSearch("abc".toByteArray())
        println(v)
        assertEquals(4, v.value)
    }

    companion object {
        const val NUM_VALID_KEYS = 1 shl 16
        const val NUM_INVALID_KEYS = 1 shl 17
    }
}
