package me.geso.kdary

/**
 * Succinct bit vector.
 */
internal class BitVector {
    private val units: AutoPool<IdType> = AutoPool()
    private var ranks: AutoArray<IdType> = AutoArray()
    private var numOnes: SizeType = 0u
    private var size: SizeType = 0u

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
     * Sets the bit at the specified index.
     *
     * @param id the index of the bit to set.
     * @param bit the value to set the bit to (true for 1, false for 0).
     */
    fun set(
        id: SizeType,
        bit: Boolean,
    ) {
        val unitId = (id / UNIT_SIZE).toInt()
        if (bit) {
            units[unitId] = units[unitId] or (1u shl (id % UNIT_SIZE).toInt())
        } else {
            units[unitId] = units[unitId] and (1u shl (id % UNIT_SIZE).toInt()).inv()
        }
    }

    val empty: Boolean
        get() = units.empty()

    /**
     * Returns the total number of 1's.
     *
     * @return the total number of 1's.
     */
    fun numOnes(): SizeType = numOnes

    /**
     * Returns the total number of bits.
     *
     * @return the total number of bits.
     */
    fun size(): SizeType = size

    /**
     * Adds a new bit to the vector.
     */
    fun append() {
        if ((size % UNIT_SIZE) == 0uL) {
            units.append(0u)
        }
        size++
    }

    /**
     * Builds the rank array.
     */
    fun build() {
        // Initialize ranks array with the size of units array
        ranks.reset(
            Array(units.size().toInt()) {
                0u
            },
        )
        numOnes = 0u
        // Populate ranks array and count the number of 1's
        for (i in 0 until units.size().toInt()) {
            ranks[i] = numOnes.toIdType()
            numOnes += popCount(units[i])
        }
    }

    /**
     * Clears all data.
     */
    fun clear() {
        units.clear()
        ranks.clear()
    }

    companion object {
        /**
         * Number of bits per unit.
         */
        private const val UNIT_SIZE = 32u

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
