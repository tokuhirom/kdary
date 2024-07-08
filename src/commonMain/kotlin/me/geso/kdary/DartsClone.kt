// Darts.kt

package me.geso.kdary

// This header assumes that <int> and <unsigned int> are 32-bit integer types.
//
// Darts-clone keeps values associated with keys. The type of the values is
// <value_type>. Note that the values must be positive integers because the
// most significant bit (MSB) of each value is used to represent whether the
// corresponding unit is a leaf or not. Also, the keys are represented by
// sequences of <char_type>s. <uchar_type> is the unsigned type of <char_type>.
typealias UCharType = UByte
typealias ValueType = Int

internal fun ULong.toIdType(): IdType = this.toUInt()

internal fun Int.toSizeType(): SizeType = this.toULong()

internal fun IdType.toSizeType(): SizeType = this.toULong()

internal fun UInt.toValueType(): ValueType = this.toInt()

// KeyType は  drts-clone では Char だが、Kotlin の Char は 16bit なので Byte にしている。
// Byte は signed 8bit。
typealias KeyType = Byte

// typedef unsigned int id_type;
typealias IdType = UInt

internal fun ValueType.toIdType(): IdType = this.toUInt()

internal fun UByte.toIdType(): IdType = this.toUInt()

// typedef int (*progress_func_type)(std::size_t, std::size_t);
// SizeType は size_t のこと。
typealias SizeType = ULong
typealias ProgressFuncType = (SizeType, SizeType) -> Int
