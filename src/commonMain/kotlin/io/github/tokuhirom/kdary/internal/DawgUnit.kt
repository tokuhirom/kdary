package io.github.tokuhirom.kdary.internal

import io.github.tokuhirom.kdary.IdType
import io.github.tokuhirom.kdary.ValueType
import io.github.tokuhirom.kdary.toValueType

/**
 * Fixed unit of Directed Acyclic Word Graph (DAWG).
 */
internal data class DawgUnit(
    private val unit: IdType,
) {
    fun unit(): IdType = unit

    fun child(): IdType = unit shr 2

    fun hasSibling(): Boolean = (unit and 1u) == 1u

    fun value(): ValueType = (unit shr 1).toValueType()

    fun isState(): Boolean = (unit and 2u) == 2u
}
