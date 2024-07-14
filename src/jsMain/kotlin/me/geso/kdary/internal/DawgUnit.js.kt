package me.geso.kdary.internal

import me.geso.kdary.IdType
import me.geso.kdary.ValueType
import me.geso.kdary.toValueType

/**
 * Fixed unit of Directed Acyclic Word Graph (DAWG).
 */
internal actual value class DawgUnit(
    private val unit: IdType,
) {
    actual fun unit(): IdType = unit

    actual fun child(): IdType = unit shr 2

    actual fun hasSibling(): Boolean = (unit and 1u) == 1u

    actual fun value(): ValueType = (unit shr 1).toValueType()

    actual fun isState(): Boolean = (unit and 2u) == 2u
}
