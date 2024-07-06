// Darts.kt

package me.geso.dartsclonekt

// typedef unsigned int id_type;
typealias id_type = UInt

// typedef int (*progress_func_type)(std::size_t, std::size_t);
typealias SizeType = ULong
typealias ProgressFuncType = (SizeType, SizeType) -> Int

