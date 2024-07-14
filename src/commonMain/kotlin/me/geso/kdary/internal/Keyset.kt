package me.geso.kdary.internal

import me.geso.kdary.SizeType
import me.geso.kdary.ValueType
import me.geso.kdary.toSizeType

internal class Keyset<T>(
    private val keys: Array<ByteArray>,
    private val values: Array<T>?,
) {
    fun numKeys(): SizeType = keys.size.toSizeType()

    fun keys(id: SizeType): ByteArray = keys[id.toInt()]

    fun keys(
        keyId: SizeType,
        charId: SizeType,
    ): UByte = keys[keyId.toInt()].getOrNull(charId.toInt())?.toUByte() ?: 0.toUByte()

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
