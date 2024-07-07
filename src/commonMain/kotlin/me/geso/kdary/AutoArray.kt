package me.geso.kdary

class AutoArray<T>(array: Array<T>? = null) {
    private var array_: Array<T>? = array

    operator fun get(id: Int): T {
        return array_?.get(id) ?: throw IndexOutOfBoundsException("Array is null")
    }

    operator fun set(id: Int, value: T) {
        array_?.set(id, value) ?: throw IndexOutOfBoundsException("Array is null")
    }

    fun isEmpty(): Boolean {
        return array_ == null
    }

    fun clear() {
        array_ = null
    }

    fun swap(other: AutoArray<T>) {
        val temp = array_
        array_ = other.array_
        other.array_ = temp
    }

    fun reset(array: Array<T>? = null) {
        AutoArray(array).swap(this)
    }
}
