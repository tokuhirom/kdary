package me.geso.kdary

class Keyset<T>(
    private val numKeys: Int,
    private val keys: Array<String>,
    private val lengths: IntArray?,
    private val values: Array<T>?,
) {
    fun numKeys(): Int {
        return numKeys
    }

    fun keys(id: Int): String {
        return keys[id]
    }

    fun keys(
        keyId: Int,
        charId: Int,
    ): Char {
        if (hasLengths() && charId >= lengths!![keyId]) {
            return '\u0000'
        }
        return keys[keyId].getOrNull(charId) ?: '\u0000'
    }

    fun hasLengths(): Boolean {
        return lengths != null
    }

    fun lengths(id: Int): Int {
        if (hasLengths()) {
            return lengths!![id]
        }
        return keys[id].length
    }

    fun hasValues(): Boolean {
        return values != null
    }

    fun values(id: Int): T {
        if (hasValues()) {
            return values!![id]
        }
        throw NoSuchElementException("No value present for key with id $id")
    }
}
