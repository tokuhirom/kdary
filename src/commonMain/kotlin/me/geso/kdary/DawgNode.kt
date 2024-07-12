package me.geso.kdary

/**
 * Node of Directed Acyclic Word Graph (DAWG).
 */
class DawgNode {
    private var child: IdType = 0u
    private var sibling: IdType = 0u
    private var label: UByte = 0u
    private var isState: Boolean = false
    private var hasSibling: Boolean = false

    fun setChild(child: IdType) {
        this.child = child
    }

    fun setSibling(sibling: IdType) {
        this.sibling = sibling
    }

    fun setValue(value: ValueType) {
        this.child = value.toIdType()
    }

    fun setLabel(label: UByte) {
        this.label = label
    }

    fun setIsState(isState: Boolean) {
        this.isState = isState
    }

    fun setHasSibling(hasSibling: Boolean) {
        this.hasSibling = hasSibling
    }

    fun child(): IdType = child

    fun sibling(): IdType = sibling

    fun value(): ValueType = child.toValueType()

    fun label(): UByte = label

    fun isState(): Boolean = isState

    fun hasSibling(): Boolean = hasSibling

    fun unit(): IdType =
        if (label == 0.toUByte()) {
            (child shl 1) or (if (hasSibling) 1u else 0u)
        } else {
            (child shl 2) or (if (isState) 2u else 0u) or (if (hasSibling) 1u else 0u)
        }
}
