package io.github.tokuhirom.kdary.result

import io.github.tokuhirom.kdary.internal.ValueType

/**
 * Result of common prefix search.
 */
data class CommonPrefixSearchResult(
    var value: ValueType,
    var length: Int,
)
