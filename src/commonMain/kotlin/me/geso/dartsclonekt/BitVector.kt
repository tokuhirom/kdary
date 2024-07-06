package me.geso.dartsclonekt

// Succinct bit vector.
class BitVector {
    private val units: MutableList<Long> = mutableListOf()
    private lateinit var ranks: Array<Long>
    private var numOnes: Long = 0
    private var size: Long = 0

    val isEmpty: Boolean
        get() = units.isEmpty()

    fun numOnes(): Long = numOnes

    fun size(): Long = size

    operator fun get(id: Long): Boolean {
        return (units[(id / UNIT_SIZE).toInt()] shr (id % UNIT_SIZE).toInt() and 1L) == 1L
    }

    fun rank(id: Long): Long {
        val unitId = (id / UNIT_SIZE).toInt()
        return ranks[unitId] + popCount(units[unitId] and (ALL_ONE_USHR shr (UNIT_SIZE - (id % UNIT_SIZE).toInt() - 1)))
    }

    fun set(
        id: Long,
        bit: Boolean,
    ) {
        val unitId = (id / UNIT_SIZE).toInt()
        if (bit) {
            units[unitId] = units[unitId] or (1L shl (id % UNIT_SIZE).toInt())
        } else {
            units[unitId] = units[unitId] and (1L shl (id % UNIT_SIZE).toInt()).inv()
        }
    }

    fun append() {
        if ((size % UNIT_SIZE) == 0L) {
            units.add(0L)
        }
        size++
    }

    fun build() {
        ranks = Array(units.size) { 0L }
        numOnes = 0
        for (i in units.indices) {
            ranks[i] = numOnes
            numOnes += popCount(units[i])
        }
    }

    fun clear() {
        units.clear()
        ranks = emptyArray()
    }

    companion object {
        // sizeof(id_type) = 4. sizeof(unsigned int) = 4.
        //  enum { UNIT_SIZE = sizeof(id_type) * 8 };
        private const val UNIT_SIZE = 32

        // ALL_ONE_USHR は ~0U と同じはずだが、-1L になってて、これはおかしい気がする。
        // TODO: 修正必要そう
        private const val ALL_ONE_USHR = -1L

        private fun popCount(unit: Long): Long {
            var u = unit
            u = (u and 0xAAAAAAAA ushr 1) + (u and 0x55555555)
            u = (u and 0xCCCCCCCC ushr 2) + (u and 0x33333333)
            u = (u ushr 4) + u and 0x0F0F0F0F
            u += u ushr 8
            u += u ushr 16
            return u and 0xFF
        }
    }
}
