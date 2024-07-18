package io.github.tokuhirom.kdary.internal

internal data class Dawg(
    val units: List<DawgUnit>,
    val labels: List<UByte>,
    val isIntersections: BitVector,
    val numStates: SizeType,
) {
    fun size(): Int = units.size

    fun numIntersections(): Int = isIntersections.numOnes().toInt()

    fun child(id: Int): Int = units[id].child()

    fun root(): Int = 0

    fun isIntersection(id: IdType): Boolean = isIntersections[id]

    fun intersectionId(id: Int): IdType = isIntersections.rank(id) - 1u

    fun isLeaf(id: Int): Boolean = label(id) == 0.toUByte()

    fun label(id: Int): UByte = labels[id]

    fun sibling(id: Int): Int = if (units[id].hasSibling()) (id + 1) else 0

    fun value(id: Int): ValueType = units[id].value()
}
