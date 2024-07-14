package me.geso.kdary.internal

import me.geso.kdary.IdType
import me.geso.kdary.ValueType
import me.geso.kdary.toValueType

/**
 * Fixed unit of Directed Acyclic Word Graph (DAWG).
 */
@JvmInline
internal value class DawgUnit(
    private val unit: IdType = 0u,
) {
    fun unit(): IdType = unit

    fun child(): IdType = unit shr 2

    fun hasSibling(): Boolean = (unit and 1u) == 1u

    fun value(): ValueType = (unit shr 1).toValueType()

    fun isState(): Boolean = (unit and 2u) == 2u
}
