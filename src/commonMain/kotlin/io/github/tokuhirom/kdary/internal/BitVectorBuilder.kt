package io.github.tokuhirom.kdary.internal

/**
 * Succinct bit vector.
 */
internal class BitVectorBuilder {
    private val units: MutableList<IdType> = mutableListOf()
    private var size: Int = 0

    /**
     * Returns the bit value at the specified index.
     *
     * @param id the index of the bit.
     * @return the bit value at the specified index.
     */
    operator fun get(id: Int): Boolean = (units[id / BitVector.UNIT_SIZE] shr id % BitVector.UNIT_SIZE and 1u) == 1u

    /**
     * Sets the bit at the specified index.
     *
     * @param id the index of the bit to set.
     * @param bit the value to set the bit to (true for 1, false for 0).
     */
    fun set(
        id: Int,
        bit: Boolean,
    ) {
        val unitId = id / BitVector.UNIT_SIZE
        if (bit) {
            units[unitId] = units[unitId] or (1u shl id % BitVector.UNIT_SIZE)
        } else {
            units[unitId] = units[unitId] and (1u shl id % BitVector.UNIT_SIZE).inv()
        }
    }

    /**
     * Returns the total number of bits.
     *
     * @return the total number of bits.
     */
    fun size(): Int = size

    /**
     * Adds a new bit to the vector.
     */
    fun append() {
        if ((size % BitVector.UNIT_SIZE) == 0) {
            units.add(0u)
        }
        size++
    }

    /**
     * Builds the rank array.
     */
    fun build(): BitVector {
        // Initialize ranks array with the size of units array
        val ranks = MutableList(units.size) { 0 }
        var numOnes: Int = 0
        // Populate ranks array and count the number of 1's
        for (i in 0 until units.size) {
            ranks[i] = numOnes
            numOnes += BitVector.popCount(units[i])
        }
        return BitVector(units, ranks, numOnes, size)
    }
}
