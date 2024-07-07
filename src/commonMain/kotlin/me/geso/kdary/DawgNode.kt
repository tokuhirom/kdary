package me.geso.kdary

typealias UCharType = UByte

class DawgNode {
    private var child: IdType = 0u
    private var sibling: IdType = 0u
    private var label: UCharType = 0u
    private var isState: Boolean = false
    private var hasSibling: Boolean = false

    fun setChild(child: IdType) {
        this.child = child
    }

    fun setSibling(sibling: IdType) {
        this.sibling = sibling
    }

    fun setValue(value: ValueType) {
        this.child = value.toUInt()
    }

    fun setLabel(label: UCharType) {
        this.label = label
    }

    fun setIsState(isState: Boolean) {
        this.isState = isState
    }

    fun setHasSibling(hasSibling: Boolean) {
        this.hasSibling = hasSibling
    }

    fun child(): IdType {
        return child
    }

    fun sibling(): IdType {
        return sibling
    }

    fun value(): ValueType {
        return child.toInt()
    }

    fun label(): UCharType {
        return label
    }

    fun isState(): Boolean {
        return isState
    }

    fun hasSibling(): Boolean {
        return hasSibling
    }

    fun unit(): IdType {
        return if (label == 0.toUByte()) {
            (child shl 1) or (if (hasSibling) 1u else 0u)
        } else {
            (child shl 2) or (if (isState) 2u else 0u) or (if (hasSibling) 1u else 0u)
        }
    }
}
