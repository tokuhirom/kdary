package io.github.tokuhirom.kdary.internal

internal typealias ValueType = Int
internal typealias IdType = UInt

/**
 * size_t in C++
 */
internal typealias SizeType = ULong

internal fun ULong.toIdType(): IdType = this.toUInt()

internal fun Int.toSizeType(): SizeType = this.toULong()

internal fun UByte.toSizeType(): SizeType = this.toULong()

internal fun IdType.toSizeType(): SizeType = this.toULong()

internal fun UInt.toValueType(): ValueType = this.toInt()

internal fun ValueType.toIdType(): IdType = this.toUInt()

internal fun UByte.toIdType(): IdType = this.toUInt()
