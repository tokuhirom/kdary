package me.geso.kdary.internal

import me.geso.kdary.IdType
import me.geso.kdary.ValueType

/**
 * Fixed unit of Directed Acyclic Word Graph (DAWG).
 */
internal expect value class DawgUnit(
    private val unit: IdType = 0u,
) {
    fun unit(): IdType

    fun child(): IdType

    fun hasSibling(): Boolean

    fun value(): ValueType

    fun isState(): Boolean
}
