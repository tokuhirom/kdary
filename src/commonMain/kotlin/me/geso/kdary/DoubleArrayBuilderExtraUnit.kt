package me.geso.kdary

/**
 * Extra unit of double-array builder.
 */
class DoubleArrayBuilderExtraUnit {
    private var prev: UInt = 0u
    private var next: UInt = 0u
    private var isFixed: Boolean = false
    private var isUsed: Boolean = false

    fun setPrev(prev: UInt) {
        this.prev = prev
    }

    fun setNext(next: UInt) {
        this.next = next
    }

    fun setIsFixed(isFixed: Boolean) {
        this.isFixed = isFixed
    }

    fun setIsUsed(isUsed: Boolean) {
        this.isUsed = isUsed
    }

    fun getPrev(): UInt = prev

    fun getNext(): UInt = next

    fun isFixed(): Boolean = isFixed

    fun isUsed(): Boolean = isUsed
}
