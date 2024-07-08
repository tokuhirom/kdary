package me.geso.kdary

class Keyset<T>(
    private val numKeys: SizeType,
    private val keys: Array<String>,
    private val lengths: Array<SizeType>?,
    private val values: Array<T>?,
) {
    fun numKeys(): SizeType = numKeys

    fun keys(id: Int): String = keys[id]

    fun keys(
        keyId: SizeType,
        charId: SizeType,
    ): Char {
        if (hasLengths() && charId >= lengths!![keyId.toInt()]) {
            return '\u0000'
        }
        return keys[keyId.toInt()].getOrNull(charId.toInt()) ?: '\u0000'
    }

    fun hasLengths(): Boolean = lengths != null

    fun lengths(id: SizeType): SizeType {
        if (hasLengths()) {
            return lengths!![id.toInt()]
        }
        return keys[id.toInt()].length.toSizeType()
    }

    fun hasValues(): Boolean = values != null

    fun values(id: SizeType): ValueType {
        if (hasValues()) {
            return values!![id.toInt()]!!.toValueType()
        }
        return id.toValueType()
    }

    private fun <T : Any> T.toValueType(): ValueType {
        return when (this) {
            is Int -> {
                return this
            }

            is UInt -> {
                return this.toInt()
            }

            is ULong -> {
                return this.toInt()
            }

            else -> {
                throw IllegalArgumentException("Unsupported type: ${this::class.simpleName}")
            }
        }
    }
}
