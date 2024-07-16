package io.github.tokuhirom.kdary.result

import io.github.tokuhirom.kdary.SizeType
import io.github.tokuhirom.kdary.ValueType

/**
 * Result of exact match search.
 */
sealed class ExactMatchSearchResult(
    open val value: ValueType,
    open val length: SizeType,
) {
    /**
     * Found.
     */
    data class Found(
        override val value: ValueType,
        override val length: SizeType,
    ) : ExactMatchSearchResult(value, length)

    /**
     * Not found.
     */
    data object NotFound : ExactMatchSearchResult(-1, 0u)
}
