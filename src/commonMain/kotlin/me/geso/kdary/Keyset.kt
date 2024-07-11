@file:OptIn(ExperimentalUnsignedTypes::class)

package me.geso.kdary

class Keyset<T>(
    // TODO numKeys は不要?
    private val numKeys: SizeType,
    private val keys: Array<UByteArray>,
    private val values: Array<T>?,
) {
    init {
        assert(numKeys == keys.size.toSizeType())
    }

    fun numKeys(): SizeType = numKeys

    fun keys(id: SizeType): UByteArray = keys[id.toInt()]

    fun keys(
        keyId: SizeType,
        charId: SizeType,
    ): UCharType = keys[keyId.toInt()].getOrNull(charId.toInt()) ?: 0.toUByte()

    fun lengths(id: SizeType): SizeType = keys[id.toInt()].size.toSizeType()

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
