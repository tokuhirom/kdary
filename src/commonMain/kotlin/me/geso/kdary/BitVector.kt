package me.geso.kdary

// Succinct bit vector.
class BitVector {
    private val units: AutoPool<IdType> = AutoPool()
    private var ranks: AutoArray<IdType> = AutoArray()
    private var numOnes: SizeType = 0u
    private var size: SizeType = 0u

    // 指定したインデックスのビット値を返す
    operator fun get(id: UInt): Boolean = (units[(id / UNIT_SIZE).toInt()] shr (id % UNIT_SIZE).toInt() and 1u) == 1u

    // 指定したインデックスまでの 1 の数を返す
    fun rank(id: SizeType): IdType {
        val unitId = (id / UNIT_SIZE).toInt()
        val offset = (id % UNIT_SIZE).toInt()
        val mask = (1u shl offset) - 1u
        return ranks[unitId] + popCount(units[unitId] and mask)
    }

    // 指定したインデックスのビットを設定する
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

    // 1 の総数を返すメソッド
    fun numOnes(): SizeType = numOnes

    // 全ビット数を返すメソッド
    fun size(): SizeType = size

//    fun toList(): List<Boolean> = (0u until size).map { get(it) }

    // 新しいビットを追加する
    fun append() {
        if ((size % UNIT_SIZE) == 0uL) {
            units.append(0u)
        }
        size++
    }

    // ランク配列を構築する
    fun build() {
        // pass new Array, tht size is units.size()
        ranks.reset(
            Array(units.size()) {
                0u
            },
        )
        numOnes = 0u
        // for (std::size_t i = 0; i < units_.size(); ++i) {
        for (i in 0 until units.size()) {
            ranks[i] = numOnes.toIdType()
            numOnes += popCount(units[i])
        }
    }

    // 全データをクリアする
    fun clear() {
        units.clear()
        ranks.clear()
    }

    companion object {
        // 1ユニットあたりのビット数
        // id_type = unsigned int
        // sizeof(id_type) = 4
        // sizeof(id_type) * 8
        private const val UNIT_SIZE = 32u

        internal fun popCount(unit: IdType): IdType {
            var u = unit
            u = ((u and 0xAAAAAAAAu) shr 1) + (u and 0x55555555u)
            u = ((u and 0xCCCCCCCCu) shr 2) + (u and 0x33333333u)
            // unit = ((unit >> 4) + unit) & 0x0F0F0F0F;
            u = ((u shr 4) + u) and 0x0F0F0F0Fu
            u += u shr 8
            u += u shr 16
            return u and 0xFFu
        }
    }
}
