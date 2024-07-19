package io.github.tokuhirom.kdary.result

/**
 * Represents the result of a traversal operation.
 *
 * @property status The status code of the traversal.
 * @property nodePos The position of the node in the traversal.
 * @property keyPos The position of the key in the traversal.
 */
data class TraverseResult(
    val status: Int,
    val nodePos: Int,
    val keyPos: Int,
)
