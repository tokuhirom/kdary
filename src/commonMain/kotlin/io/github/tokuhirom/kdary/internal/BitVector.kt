package io.github.tokuhirom.kdary.internal

import io.github.tokuhirom.kdary.IdType
import io.github.tokuhirom.kdary.SizeType

/**
 * Succinct bit vector.
 */
internal data class BitVector(
    private val units: List<IdType>,
    private val ranks: Array<IdType>,
    private val numOnes: SizeType = 0u,
    private val size: SizeType = 0u,
) {
    /**
     * Returns the bit value at the specified index.
     *
     * @param id the index of the bit.
     * @return the bit value at the specified index.
     */
    operator fun get(id: UInt): Boolean = (units[(id / UNIT_SIZE).toInt()] shr (id % UNIT_SIZE).toInt() and 1u) == 1u

    /**
     * Returns the number of 1's up to the specified index.
     *
     * @param id the index up to which to count the number of 1's.
     * @return the number of 1's up to the specified index.
     */
    fun rank(id: SizeType): IdType {
        val unitId = id / UNIT_SIZE
        val offset: SizeType = UNIT_SIZE - (id % UNIT_SIZE) - 1u
        val mask: UInt = 0U.inv() shr offset.toInt()
        return ranks[unitId.toInt()] +
            popCount(units[unitId.toInt()] and mask)
    }

    /**
     * Returns the total number of 1's.
     *
     * @return the total number of 1's.
     */
    fun numOnes(): SizeType = numOnes

    companion object {
        /**
         * Number of bits per unit.
         */
        internal const val UNIT_SIZE = 32u

        /**
         * Returns the population count (number of 1's) in the given unit.
         *
         * @param unit the unit to count the number of 1's in.
         * @return the population count of the unit.
         */
        internal fun popCount(unit: IdType): IdType {
            var u = unit
            u = ((u and 0xAAAAAAAAu) shr 1) + (u and 0x55555555u)
            u = ((u and 0xCCCCCCCCu) shr 2) + (u and 0x33333333u)
            u = ((u shr 4) + u) and 0x0F0F0F0Fu
            u += u shr 8
            u += u shr 16
            return u and 0xFFu
        }
    }
}
