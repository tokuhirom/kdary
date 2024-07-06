// Darts.kt

package me.geso.dartsclonekt

// This header assumes that <int> and <unsigned int> are 32-bit integer types.
//
// Darts-clone keeps values associated with keys. The type of the values is
// <value_type>. Note that the values must be positive integers because the
// most significant bit (MSB) of each value is used to represent whether the
// corresponding unit is a leaf or not. Also, the keys are represented by
// sequences of <char_type>s. <uchar_type> is the unsigned type of <char_type>.
typealias ValueType = Int

// typedef unsigned int id_type;
typealias IdType = UInt

// typedef int (*progress_func_type)(std::size_t, std::size_t);
typealias SizeType = ULong
typealias ProgressFuncType = (SizeType, SizeType) -> Int
