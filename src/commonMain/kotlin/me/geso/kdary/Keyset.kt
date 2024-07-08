@file:OptIn(ExperimentalUnsignedTypes::class)

package me.geso.kdary

class Keyset<T>(
    private val numKeys: SizeType,
    private val keys: Array<UByteArray>,
    private val lengths: Array<SizeType>?,
    private val values: Array<T>?,
) {
    fun numKeys(): SizeType = numKeys

    fun keys(id: SizeType): UByteArray = keys[id.toInt()]

    fun keys(
        keyId: SizeType,
        charId: SizeType,
    ): UCharType {
        if (hasLengths() && charId >= lengths!![keyId.toInt()]) {
            return 0.toUByte()
        }
        return keys[keyId.toInt()].getOrNull(charId.toInt()) ?: 0.toUByte()
    }

    fun hasLengths(): Boolean = lengths != null

    fun lengths(id: SizeType): SizeType {
        if (hasLengths()) {
            return lengths!![id.toInt()]
        }
        return keys[id.toInt()].size.toSizeType()
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
