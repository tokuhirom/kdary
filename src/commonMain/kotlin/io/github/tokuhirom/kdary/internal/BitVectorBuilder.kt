package io.github.tokuhirom.kdary.internal

import io.github.tokuhirom.kdary.IdType
import io.github.tokuhirom.kdary.SizeType
import io.github.tokuhirom.kdary.toIdType
import io.github.tokuhirom.kdary.toSizeType

/**
 * Succinct bit vector.
 */
internal class BitVectorBuilder {
    private val units: MutableList<IdType> = mutableListOf()
    private var size: SizeType = 0u

    /**
     * Returns the bit value at the specified index.
     *
     * @param id the index of the bit.
     * @return the bit value at the specified index.
     */
    operator fun get(id: UInt): Boolean = (units[(id / BitVector.UNIT_SIZE).toInt()] shr (id % BitVector.UNIT_SIZE).toInt() and 1u) == 1u

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
        val unitId = (id / BitVector.UNIT_SIZE).toInt()
        if (bit) {
            units[unitId] = units[unitId] or (1u shl (id % BitVector.UNIT_SIZE).toInt())
        } else {
            units[unitId] = units[unitId] and (1u shl (id % BitVector.UNIT_SIZE).toInt()).inv()
        }
    }

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
        if ((size % BitVector.UNIT_SIZE) == 0uL) {
            units.add(0u)
        }
        size++
    }

    /**
     * Builds the rank array.
     */
    fun build(): BitVector {
        // Initialize ranks array with the size of units array
        val ranks =
            Array(units.size.toSizeType().toInt()) {
                0u
            }
        var numOnes: SizeType = 0u
        // Populate ranks array and count the number of 1's
        for (i in 0 until units.size.toSizeType().toInt()) {
            ranks[i] = numOnes.toIdType()
            numOnes += BitVector.popCount(units[i])
        }
        return BitVector(units, ranks, numOnes, size)
    }
}
