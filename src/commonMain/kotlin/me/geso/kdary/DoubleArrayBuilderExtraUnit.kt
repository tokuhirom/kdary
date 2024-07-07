package me.geso.kdary

/**
 * Extra unit of double-array builder.
 */
class DoubleArrayBuilderExtraUnit {
    private var prev: IdType = 0u
    private var next: IdType = 0u
    private var isFixed: Boolean = false
    private var isUsed: Boolean = false

    fun setPrev(prev: IdType) {
        this.prev = prev
    }

    fun setNext(next: IdType) {
        this.next = next
    }

    fun setIsFixed(isFixed: Boolean) {
        this.isFixed = isFixed
    }

    fun setIsUsed(isUsed: Boolean) {
        this.isUsed = isUsed
    }

    fun prev(): IdType = prev

    fun next(): IdType = next

    fun isFixed(): Boolean = isFixed

    fun isUsed(): Boolean = isUsed
}
