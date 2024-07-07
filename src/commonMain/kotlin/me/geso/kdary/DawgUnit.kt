package me.geso.kdary

/**
 * Fixed unit of Directed Acyclic Word Graph (DAWG).
 */
class DawgUnit(
    private var unit: IdType = 0u,
) {
    constructor(unit: DawgUnit) : this(unit.unit)

    fun unit(): IdType = unit

    fun setUnit(unit: IdType): DawgUnit {
        this.unit = unit
        return this
    }

    fun child(): IdType = unit shr 2

    fun hasSibling(): Boolean = (unit and 1u) == 1u

    fun value(): ValueType = (unit shr 1).toInt()

    fun isState(): Boolean = (unit and 2u) == 2u
}