package me.geso.kdary.internal

import me.geso.kdary.IdType
import me.geso.kdary.SizeType
import me.geso.kdary.ValueType
import me.geso.kdary.toSizeType

internal data class Dawg(
    val units: List<DawgUnit>,
    val labels: List<UByte>,
    val isIntersections: BitVector,
    val numStates: SizeType,
) {
    fun size(): SizeType = units.size.toSizeType()

    fun numIntersections(): Int = isIntersections.numOnes().toInt()

    fun child(id: IdType): IdType = units[id.toInt()].child()

    fun root(): IdType = 0u

    fun isIntersection(id: IdType): Boolean = isIntersections[id]

    fun intersectionId(id: IdType): IdType = isIntersections.rank(id.toSizeType()) - 1u

    fun isLeaf(id: IdType): Boolean = label(id) == 0.toUByte()

    fun label(id: IdType): UByte = labels[id.toInt()]

    fun sibling(id: IdType): IdType = if (units[id.toInt()].hasSibling()) (id + 1u) else 0u

    fun value(id: IdType): ValueType = units[id.toInt()].value()
}
