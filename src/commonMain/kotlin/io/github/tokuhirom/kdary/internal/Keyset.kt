package io.github.tokuhirom.kdary.internal

internal class Keyset(
    private val keys: List<ByteArray>,
    private val values: List<Int>?,
) {
    fun numKeys(): SizeType = keys.size.toSizeType()

    fun keys(id: SizeType): ByteArray = keys[id.toInt()]

    fun keys(
        keyId: SizeType,
        charId: SizeType,
    ): UByte = keys[keyId.toInt()].getOrNull(charId.toInt())?.toUByte() ?: 0.toUByte()

    fun hasValues(): Boolean = values != null

    fun values(id: SizeType): ValueType {
        if (values != null) {
            return values[id.toInt()]
        }
        return id.toValueType()
    }
}
