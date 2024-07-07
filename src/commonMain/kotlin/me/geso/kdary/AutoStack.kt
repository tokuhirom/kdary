package me.geso.kdary

class AutoStack<T> {
    private val pool = AutoPool<T>()

    val size: Int
        get() = pool.size()

    fun isEmpty(): Boolean = pool.empty()

    fun top(): T = pool[size - 1]

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
