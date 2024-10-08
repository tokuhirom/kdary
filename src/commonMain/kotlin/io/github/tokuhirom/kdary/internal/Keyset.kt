package io.github.tokuhirom.kdary.internal

internal class Keyset(
    private val keys: List<ByteArray>,
    private val values: List<Int>?,
) {
    fun numKeys(): Int = keys.size

    fun keys(id: Int): ByteArray = keys[id]

    fun keys(
        keyId: Int,
        charId: Int,
    ): UByte = keys[keyId].getOrNull(charId)?.toUByte() ?: 0.toUByte()

    fun hasValues(): Boolean = values != null

    fun values(id: Int): ValueType {
        if (values != null) {
            return values[id]
        }
        return id
    }
}
