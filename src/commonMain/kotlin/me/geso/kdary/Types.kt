package me.geso.kdary

typealias ValueType = Int
typealias IdType = UInt

/**
 * size_t in C++
 */
typealias SizeType = ULong

internal fun ULong.toIdType(): IdType = this.toUInt()

internal fun Int.toSizeType(): SizeType = this.toULong()

internal fun IdType.toSizeType(): SizeType = this.toULong()

internal fun UInt.toValueType(): ValueType = this.toInt()

internal fun ValueType.toIdType(): IdType = this.toUInt()

internal fun UByte.toIdType(): IdType = this.toUInt()

typealias ProgressFuncType = (SizeType, SizeType) -> Int
