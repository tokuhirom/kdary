package me.geso.kdary.internal

import me.geso.kdary.IdType
import me.geso.kdary.ValueType

/**
 * DoubleArrayUnit is a type for double array units, and is essentially a wrapper for IdType.
 */
internal expect value class DoubleArrayUnit(
    val unit: IdType,
) {
    /**
     * Checks if the unit is a leaf unit directly derived from the unit (returns true) or not (returns false).
     *
     * @return true if the unit is a leaf unit, false otherwise.
     */
    fun hasLeaf(): Boolean

    /**
     * Returns the value stored in the unit. This is only available if the unit is a leaf unit.
     *
     * @return The value stored in the unit.
     */
    fun value(): ValueType

    /**
     * Returns the label associated with the unit. Leaf units always return an invalid label.
     * For this functionality, the label() of a leaf unit returns an IdType with the MSB set to 1.
     *
     * @return The label associated with the unit.
     */
    fun label(): IdType

    /**
     * Returns the offset to the unit derived from the unit.
     *
     * @return The offset to the derived unit.
     */
    fun offset(): IdType
}
