package io.github.tokuhirom.kdary.result

import io.github.tokuhirom.kdary.internal.SizeType

data class TraverseResult(
    val status: Int,
    val nodePos: SizeType? = null,
    val keyPos: SizeType? = null,
)
