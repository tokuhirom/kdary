package me.geso.kdary

// Memory management of array.
class AutoArray<T>(
    array: Array<T>? = null,
) {
    private var arrayProp: Array<T>? = array

    operator fun get(id: Int): T = arrayProp?.get(id) ?: throw IndexOutOfBoundsException("Array is null")

    operator fun set(
        id: Int,
        value: T,
    ) {
        arrayProp?.set(id, value) ?: throw IndexOutOfBoundsException("Array is null")
    }

    fun isEmpty(): Boolean = arrayProp == null

    fun clear() {
        arrayProp = null
    }

    fun swap(other: AutoArray<T>) {
        val temp = arrayProp
        arrayProp = other.arrayProp
        other.arrayProp = temp
    }

    fun reset(array: Array<T>? = null) {
        AutoArray(array).swap(this)
    }
}
