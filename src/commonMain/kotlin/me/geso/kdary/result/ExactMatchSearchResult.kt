package me.geso.kdary.result

import me.geso.kdary.SizeType
import me.geso.kdary.ValueType

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
