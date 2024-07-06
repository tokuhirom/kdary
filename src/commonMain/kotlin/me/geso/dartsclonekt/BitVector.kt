package me.geso.dartsclonekt

// Succinct bit vector.
class BitVector {
    private val units: MutableList<UInt> = mutableListOf() // UInt 型に変更
    private lateinit var ranks: Array<UInt>
    private var numOnes: UInt = 0u
    private var size: UInt = 0u

    val isEmpty: Boolean
        get() = units.isEmpty()

    // 1 の総数を返すメソッド
    fun numOnes(): UInt = numOnes

    // 全ビット数を返すメソッド
    fun size(): UInt = size

    // 指定したインデックスのビット値を返す
    operator fun get(id: UInt): Boolean {
        return (units[(id / UNIT_SIZE).toInt()] shr (id % UNIT_SIZE).toInt() and 1u) == 1u
    }

    // 指定したインデックスまでの 1 の数を返す
    fun rank(id: UInt): UInt {
        val unitId = (id / UNIT_SIZE).toInt()
        val offset = (id % UNIT_SIZE).toInt()
        val mask = (1u shl offset) - 1u
        return ranks[unitId] + popCount(units[unitId] and mask)
    }

    // 指定したインデックスのビットを設定する
    fun set(
        id: UInt,
        bit: Boolean,
    ) {
        val unitId = (id / UNIT_SIZE).toInt()
        if (bit) {
            units[unitId] = units[unitId] or (1u shl (id % UNIT_SIZE).toInt())
        } else {
            units[unitId] = units[unitId] and (1u shl (id % UNIT_SIZE).toInt()).inv()
        }
    }

    fun toList(): List<Boolean> {
        return (0u until size).map { get(it) }
    }

    // 新しいビットを追加する
    fun append() {
        if ((size % UNIT_SIZE) == 0u) {
            units.add(0u)
        }
        size++
    }

    // ランク配列を構築する
    fun build() {
        ranks = Array(units.size) { 0u }
        numOnes = 0u
        for (i in units.indices) {
            ranks[i] = numOnes
            println(units[i])
            numOnes += popCount(units[i])
        }
    }

    // 全データをクリアする
    fun clear() {
        units.clear()
        ranks = emptyArray()
        numOnes = 0u
        size = 0u
    }

    companion object {
        // 1ユニットあたりのビット数
        // id_type = unsigned int
        // sizeof(id_type) = 4
        // sizeof(id_type) * 8
        private const val UNIT_SIZE = 32u

        internal fun popCount(unit: UInt): UInt {
            var u = unit
            u = (u and 0xAAAAAAAAu shr 1) + (u and 0x55555555u)
            u = (u and 0xCCCCCCCCu shr 2) + (u and 0x33333333u)
            u = (u shr 4) + (u and 0x0F0F0F0Fu)
            u += u shr 8
            u += u shr 16
            return u and 0xFFu
        }
    }
}
