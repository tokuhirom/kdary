package me.geso.kdary.result

import me.geso.kdary.SizeType
import me.geso.kdary.ValueType

/**
 * Result of common prefix search.
 */
data class CommonPrefixSearchResult(
    var value: ValueType,
    var length: SizeType,
)
