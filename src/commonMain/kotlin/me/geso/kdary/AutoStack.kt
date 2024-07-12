package me.geso.kdary

/**
 * Memory management of stack.
 */
internal class AutoStack<T> {
    private val pool = mutableListOf<T>()

    fun top(): T = pool[(size() - 1u).toInt()]

    fun empty(): Boolean = pool.isEmpty()

    fun size(): SizeType = pool.size()

    fun push(value: T) {
        pool.pushBack(value)
    }

    fun pop() {
        pool.popBack()
    }

    fun clear() {
        pool.clear()
    }
}
