package me.geso.kdary.internal

// Memory management of array.
internal class AutoArray<T>(
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

    fun reset(array: Array<T>) {
        this.arrayProp = array
    }
}
