package io.github.tokuhirom.kdary.internal

/**
 * Succinct bit vector.
 */
internal data class BitVector(
    private val units: List<IdType>,
    private val ranks: List<Int>,
    private val numOnes: Int,
    private val size: Int,
) {
    /**
     * Returns the bit value at the specified index.
     *
     * @param id the index of the bit.
     * @return the bit value at the specified index.
     */
    operator fun get(id: Int): Boolean = (units[id / UNIT_SIZE] shr (id % UNIT_SIZE) and 1u) == 1u

    /**
     * Returns the number of 1's up to the specified index.
     *
     * @param id the index up to which to count the number of 1's.
     * @return the number of 1's up to the specified index.
     */
    fun rank(id: Int): Int {
        val unitId = id / UNIT_SIZE
        val offset = UNIT_SIZE - (id % UNIT_SIZE) - 1
        val mask: UInt = 0U.inv() shr offset
        return ranks[unitId] + popCount(units[unitId] and mask)
    }

    /**
     * Returns the total number of 1's.
     *
     * @return the total number of 1's.
     */
    fun numOnes(): Int = numOnes

    companion object {
        /**
         * Number of bits per unit.
         */
        internal const val UNIT_SIZE = 32

        /**
         * Returns the population count (number of 1's) in the given unit.
         *
         * @param unit the unit to count the number of 1's in.
         * @return the population count of the unit.
         */
        internal fun popCount(unit: IdType): Int {
            var u = unit
            u = ((u and 0xAAAAAAAAu) shr 1) + (u and 0x55555555u)
            u = ((u and 0xCCCCCCCCu) shr 2) + (u and 0x33333333u)
            u = ((u shr 4) + u) and 0x0F0F0F0Fu
            u += u shr 8
            u += u shr 16
            return (u and 0xFFu).toInt()
        }
    }
}
