package me.geso.kdary

/**
 * Fixed unit of Directed Acyclic Word Graph (DAWG).
 */
class DawgUnit(
    private val unit: IdType = 0u,
) {
    constructor(unit: DawgUnit) : this(unit.unit)

    fun unit(): IdType = unit

    fun child(): IdType = unit shr 2

    fun hasSibling(): Boolean = (unit and 1u) == 1u

    fun value(): ValueType = (unit shr 1).toValueType()

    fun isState(): Boolean = (unit and 2u) == 2u
}
