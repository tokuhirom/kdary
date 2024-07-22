package io.github.tokuhirom.kdary

import io.github.tokuhirom.kdary.internal.ValueType
import io.github.tokuhirom.kdary.internal.toSizeType
import io.github.tokuhirom.kdary.result.ExactMatchSearchResult
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

fun byteArrayToString(byteArray: ByteArray): String = byteArray.decodeToString()

class DoubleArrayTest {
    private val random = Random(seed = 0)
    private val validKeys = generateValidKeys(NUM_VALID_KEYS, random)
    private val invalidKeys = generateInvalidKeys(NUM_INVALID_KEYS, validKeys, random)
    private val testData = buildData()
    private val keys: List<ByteArray> = testData.keys
    private val values: List<ValueType> = testData.values

    private fun buildData(): TestData {
        val keys: MutableList<ByteArray> = mutableListOf()
        val values: MutableList<ValueType> = mutableListOf()

        for ((keyId, key) in validKeys.sortedBy { byteArrayToString(it) }.withIndex()) {
            keys.add(key)
            values.add(keyId)
        }
        keys.forEachIndexed { index, key ->
            if (index <= 3 || index >= NUM_VALID_KEYS - 3) {
                println("index: $index, key: ${byteArrayToString(keys[index])}, value: ${values[index]}")
            }
        }
        assertEquals(0, values[0])

        // Check the duplication between validKeys and invalidKeys.
        val validKeyStrings = validKeys.map { byteArrayToString(it) }.toSet()
        val invalidKeyStrings = invalidKeys.map { byteArrayToString(it) }.toSet()
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
        val keys = mutableSetOf<String>()
        while (keys.size < numKeys) {
            val key = ByteArray(1 + (0..7).random())
            for (i in key.indices) {
                key[i] = ('A'.code + (0..25).random(random)).toByte()
            }
            keys.add(byteArrayToString(key))
        }
        return keys.map { it.encodeToByteArray() }.toSet()
    }

    private fun generateInvalidKeys(
        numInvalidKeys: Int,
        validKeys: Set<ByteArray>,
        random: Random,
    ): Set<ByteArray> {
        val keys = mutableSetOf<String>()
        val validKeyStrings = validKeys.map { byteArrayToString(it) }.toSet()
        while (keys.size < numInvalidKeys) {
            val key = ByteArray(1 + (0..7).random(random))
            for (i in key.indices) {
                key[i] = ('A'.code + (0..25).random(random)).toByte()
            }
            if (!validKeyStrings.contains(byteArrayToString(key))) {
                keys.add(byteArrayToString(key))
            }
        }
        return keys.map { it.encodeToByteArray() }.toSet()
    }

    @Test
    fun buildWithKeys() {
        val dic = KDary.build(keys)
        testDic(dic, keys, values, invalidKeys)
    }

    @Test
    fun buildWithKeysValues() {
        val dic = KDary.build(keys, values)
        testDic(dic, keys, values, invalidKeys)
    }

    @Test
    fun buildWithKeysAndRandomValues() {
        val newValues = values.map { (0..9).random(random) }

        val dic = KDary.build(keys, newValues)
        testDic(dic, keys, newValues, invalidKeys)
    }

    @Test
    fun saveAndOpen() {
        val dic = KDary.build(keys, values)
        saveKDary(dic, "test-darts.dic")

        val dicCopy = loadKDary("test-darts.dic")

        assertEquals(dic.array.size, dicCopy.array.size)
        println(dic.array.size)
        dic.array.forEachIndexed { index, doubleArrayUnit ->
            assertEquals(dicCopy.array[index], doubleArrayUnit, "index=$index")
        }
        testDic(dicCopy, keys, values, invalidKeys)
    }

    @Test
    fun fromByteArrayAndToByteArray() {
        val dic = KDary.build(keys, values)
        val gotBa = dic.toByteArray()
        val dicCopy = KDary.fromByteArray(gotBa)

        assertEquals(dic.array.size, dicCopy.array.size)
        println(dic.array.size)
        dic.array.forEachIndexed { index, doubleArrayUnit ->
            assertEquals(dicCopy.array[index], doubleArrayUnit, "index=$index")
        }
        testDic(dicCopy, keys, values, invalidKeys)
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun fromByteArrayAndToByteArrayBasic() {
        val byteArray =
            byteArrayOf(
                0xAA.toByte(),
                0xBB.toByte(),
                0xCC.toByte(),
                0xDD.toByte(),
                //
                0xEE.toByte(),
                0xFF.toByte(),
                0x1A.toByte(),
                0x2B.toByte(),
                //
                0x3C.toByte(),
                0x4D.toByte(),
                0x5E.toByte(),
                0x6F.toByte(),
                //
                0x7A.toByte(),
                0x8B.toByte(),
                0x9C.toByte(),
                0xFD.toByte(),
            )
        val kdary = KDary.fromByteArray(byteArray)
        val got = kdary.toByteArray()
        println("Original bytes: ${byteArray.joinToString { it.toHexString(HexFormat.Default) }}")
        println("Converted bytes: ${got.joinToString { it.toHexString(HexFormat.Default) }}")
        assertEquals(got.size, byteArray.size)
        for (index in got.indices) {
            assertEquals(got[index], byteArray[index])
        }
    }

    private fun testDic(
        dic: KDary,
        keys: List<ByteArray>,
        values: List<ValueType>,
        invalidKeys: Set<ByteArray>,
    ) {
        for (i in keys.indices) {
            val result = dic.exactMatchSearch(keys[i])
            assertTrue(result is ExactMatchSearchResult.Found)
            assertEquals(result.value, values[i])
        }

        invalidKeys.forEach { invalidKey ->
            val result = dic.exactMatchSearch(invalidKey)
            assertTrue(result is ExactMatchSearchResult.NotFound)
        }
    }

    @Test
    fun testTraverse() {
        val dic = KDary.build(keys, values)

        for (i in keys.indices) {
            val key = keys[i]
            var id = 0
            var keyPos = 0
            var result = 0
            for (j in 0uL until keys[i].size.toSizeType()) {
                val r = dic.traverse(key, id, keyPos)
                assertTrue(r.status != -2)
                result = r.status
            }
            assertEquals(result, values[i])
        }

        for (invalidKey in invalidKeys) {
            var id = 0
            var keyPos = 0
            var result = 0
            for (i in 0uL until invalidKey.size.toSizeType()) {
                val r = dic.traverse(invalidKey, id, keyPos)
                result = r.status
                if (result == -2) {
                    break
                }
            }
            assertTrue(result < 0)
        }
    }

    @Test
    fun simple() {
        val dic = KDary.build(listOf("abc".encodeToByteArray()), listOf(4))
        println("----------")
        val v = dic.exactMatchSearch("abc".encodeToByteArray())
        println(v)
        assertTrue(v is ExactMatchSearchResult.Found)
        assertEquals(4, v.value)
    }

    @Test
    fun testCommonPrefixSearchSimple() {
        val dic =
            KDary.build(
                listOf(
                    "京都".encodeToByteArray(),
                    "東".encodeToByteArray(),
                    "東京都".encodeToByteArray(),
                ),
                listOf(5963, 4649, 7676),
            )
        println("----------")
        val result = dic.commonPrefixSearch("東京都庁".encodeToByteArray())
        println(result)
        assertEquals(2, result.size)
        assertEquals(4649, result[0].value)
        assertEquals(7676, result[1].value)
    }

    @Test
    fun testCommonPrefixSearch() {
        val dic = KDary.build(keys, values)
        for (i in keys.indices) {
            val key: ByteArray = keys[i]
            val results = dic.commonPrefixSearch(key)
            assertTrue(results.isNotEmpty())
            assertTrue(results.size < 10)
            assertEquals(results[results.size - 1].value, values[i])
            assertEquals(results[results.size - 1].length.toInt(), key.size)
        }

        for (invalidKey in invalidKeys) {
            val results = dic.commonPrefixSearch(invalidKey)
            assertTrue(results.size < 10)

            if (results.isNotEmpty()) {
                assertTrue(results[results.size - 1].value != -1)
                assertTrue(results[results.size - 1].length.toInt() < invalidKey.size)
            }
        }
    }

    @Test
    fun testTotalSize() {
        val dic = KDary.build(keys, values)
        assertTrue(800000 < dic.totalSize())
    }

    @Test
    fun testProgressFunc() {
        val result = mutableListOf<Int>()
        KDary.build(keys, values) {
            result.add(it)
        }
        assertEquals((1..keys.size + 1).toList(), result)
    }

    companion object {
        const val NUM_VALID_KEYS = 1 shl 16
        const val NUM_INVALID_KEYS = 1 shl 17
    }
}
