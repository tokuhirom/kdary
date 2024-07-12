package me.geso.kdary

/**
 * Memory management of stack.
 */
internal class AutoStack<T> {
    private val pool = mutableListOf<T>()

    fun top(): T = pool[(size() - 1u).toInt()]

    fun empty(): Boolean = pool.isEmpty()

    fun size(): SizeType = pool.size.toSizeType()

    fun push(value: T) {
        pool.add(value)
    }

    fun pop() {
        pool.removeLast()
    }

    fun clear() {
        pool.clear()
    }
}
