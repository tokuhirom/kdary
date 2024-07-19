/**
 * Result of exact match search.
 */
package io.github.tokuhirom.kdary.result

import io.github.tokuhirom.kdary.internal.ValueType

/**
 * Represents the result of an exact match search.
 * This sealed class has two possible outcomes:
 * - [Found]: Indicates that the key was found, containing the value and length.
 * - [NotFound]: Indicates that the key was not found.
 */
sealed class ExactMatchSearchResult(
    open val value: ValueType,
    open val length: Int,
) {
    /**
     * Represents a successful search result where the key was found.
     *
     * @property value The value associated with the found key.
     * @property length The length of the found key.
     */
    data class Found(
        override val value: ValueType,
        override val length: Int,
    ) : ExactMatchSearchResult(value, length)

    /**
     * Represents a failed search result where the key was not found.
     */
    data object NotFound : ExactMatchSearchResult(-1, 0)
}
