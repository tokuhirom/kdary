package io.github.tokuhirom.kdary.result

import io.github.tokuhirom.kdary.internal.ValueType

/**
 * Result of exact match search.
 */
sealed class ExactMatchSearchResult(
    open val value: ValueType,
    open val length: Int,
) {
    /**
     * Found.
     */
    data class Found(
        override val value: ValueType,
        override val length: Int,
    ) : ExactMatchSearchResult(value, length)

    /**
     * Not found.
     */
    data object NotFound : ExactMatchSearchResult(-1, 0)
}
