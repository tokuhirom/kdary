@file:OptIn(ExperimentalUnsignedTypes::class)

package me.geso.kdary

import kotlin.random.Random
import kotlin.test.Test
// static const std::size_t NUM_VALID_KEYS = 1 << 16;
// static const std::size_t NUM_INVALID_KEYS = 1 << 17;
//
// std::set<std::string> valid_keys;
// generate_valid_keys(NUM_VALID_KEYS, &valid_keys);
//
// std::set<std::string> invalid_keys;
// generate_invalid_keys(NUM_INVALID_KEYS, valid_keys, &invalid_keys);

class DoubleArrayImplTest {
    private val validKeys = mutableSetOf<UByteArray>()
    private val invalidKeys = mutableSetOf<UByteArray>()
    private val keys: MutableList<UByteArray> = mutableListOf()
    private val lengths: MutableList<SizeType> = mutableListOf()
    private val values: MutableList<ValueType> = mutableListOf()

    init {
        val random = Random(0)
        generateValidKeys(NUM_VALID_KEYS, validKeys, random)
        generateInvalidKeys(NUM_INVALID_KEYS, validKeys, invalidKeys, random)

        /*
  std::size_t key_id = 0;
  for (std::set<std::string>::const_iterator it = valid_keys.begin();
      it != valid_keys.end(); ++it, ++key_id) {
    keys[key_id] = it->c_str();
    lengths[key_id] = it->length();
    values[key_id] = static_cast<typename T::value_type>(key_id);
  }
         */
        for ((keyId, key) in validKeys.sortedBy { String(it.toByteArray()) }.withIndex()) {
            keys.add(key)
            lengths.add(key.size.toSizeType())
            values.add(keyId)
        }
    }

    private fun generateValidKeys(
        numKeys: Int,
        validKeys: MutableSet<UByteArray>,
        random: Random,
    ) {
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
        while (validKeys.size < numKeys) {
            val key = UByteArray(1 + (0..7).random())
            for (i in key.indices) {
                key[i] = ('A'.code + (0..25).random(random)).toUByte()
            }
            validKeys.add(key)
        }
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
        validKeys: MutableSet<UByteArray>,
        invalidKeys: MutableSet<UByteArray>,
        random: Random,
    ) {
        while (invalidKeys.size < numInvalidKeys) {
            val key = UByteArray(1 + (0..7).random(random))
            for (i in key.indices) {
                key[i] = ('A'.code + (0..25).random(random)).toUByte()
            }
            val generatedKey = key.toByteArray().toUByteArray()
            if (!validKeys.contains(generatedKey)) {
                invalidKeys.add(key)
            }
        }
    }

    @Test
    fun `build() with keys`() {
        val dic = DoubleArray()
        dic.build(keys.size.toSizeType(), keys.toTypedArray())
        testDic(dic, keys, lengths, values, invalidKeys)
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
        keys: MutableList<UByteArray>,
        lengths: MutableList<SizeType>,
        values: MutableList<ValueType>,
        invalidKeys: MutableSet<UByteArray>,
    ) {
        for (i in keys.indices) {
            val result = dic.exactMatchSearch(keys[i])
            assert(result.value == values[i])

//            dic.exactMatchSearch(keys[i], result)
//            assert(result.value == values[i])
//            assert(result.length == lengths[i])
//
//            dic.exactMatchSearch(keys[i], value, lengths[i])
//            assert(value == values[i])
//
//            dic.exactMatchSearch(keys[i], result, lengths[i])
//            assert(result.value == values[i])
//            assert(result.length == lengths[i])
        }
    }

    companion object {
        const val NUM_VALID_KEYS = 1 shl 16
        const val NUM_INVALID_KEYS = 1 shl 17
    }
}
