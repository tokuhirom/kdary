package io.github.tokuhirom.kdary.result

import io.github.tokuhirom.kdary.internal.ValueType

/**
 * Represents the result of a common prefix search.
 *
 * @property value The value associated with the found prefix.
 * @property length The length of the found prefix.
 */
data class CommonPrefixSearchResult(
    var value: ValueType,
    var length: Int,
)
