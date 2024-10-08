package io.github.tokuhirom.kdary.internal

internal data class Dawg(
    val units: List<DawgUnit>,
    val labels: List<UByte>,
    val isIntersections: BitVector,
) {
    fun size(): Int = units.size

    fun numIntersections(): Int = isIntersections.numOnes()

    fun child(id: Int): Int = units[id].child()

    fun root(): Int = 0

    fun isIntersection(id: Int): Boolean = isIntersections[id]

    fun intersectionId(id: Int): Int = isIntersections.rank(id) - 1

    fun isLeaf(id: Int): Boolean = label(id) == 0.toUByte()

    fun label(id: Int): UByte = labels[id]

    fun sibling(id: Int): Int = if (units[id].hasSibling()) (id + 1) else 0

    fun value(id: Int): ValueType = units[id].value()
}
