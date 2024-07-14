package me.geso.kdary.internal

import me.geso.kdary.IdType
import me.geso.kdary.ValueType
import me.geso.kdary.toValueType

/**
 * DoubleArrayUnit is a type for double array units, and is essentially a wrapper for IdType.
 */
internal actual value class DoubleArrayUnit(
    val unit: IdType,
) {
    /**
     * Checks if the unit is a leaf unit directly derived from the unit (returns true) or not (returns false).
     *
     * @return true if the unit is a leaf unit, false otherwise.
     */
    actual fun hasLeaf(): Boolean = ((unit.toInt() shr 8) and 1) == 1

    /**
     * Returns the value stored in the unit. This is only available if the unit is a leaf unit.
     *
     * @return The value stored in the unit.
     */
    actual fun value(): ValueType = (unit and ((1u shl 31) - 1u)).toValueType()

    /**
     * Returns the label associated with the unit. Leaf units always return an invalid label.
     * For this functionality, the label() of a leaf unit returns an IdType with the MSB set to 1.
     *
     * @return The label associated with the unit.
     */
    actual fun label(): IdType = unit and ((1u shl 31) or 0xFFu)

    /**
     * Returns the offset to the unit derived from the unit.
     *
     * @return The offset to the derived unit.
     */
    actual fun offset(): IdType {
        val shiftedUnit = (unit shr 10).toInt()
        val shiftedMask = ((unit and (1u shl 9)) shr 6).toInt()
        return (shiftedUnit shl shiftedMask).toUInt()
    }
}
