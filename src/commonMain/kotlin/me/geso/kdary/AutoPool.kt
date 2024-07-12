package me.geso.kdary

/**
 * Memory management of resizable array.
 */
internal class AutoPool<T> {
    // TODO: use MutableList directly.
    // This implementation is based on MutableList.
    private var buf: MutableList<T> = mutableListOf()

    operator fun get(id: Int): T = buf[id]

    operator fun set(
        id: Int,
        value: T,
    ) {
        buf[id] = value
    }

    fun empty(): Boolean = buf.isEmpty()

    fun size(): SizeType = buf.size.toSizeType()

    fun clear() {
        buf.clear()
    }

    fun pushBack(value: T) {
        append(value)
    }

    fun popBack() {
        buf.removeLast()
    }

    fun append(value: T) {
        buf.add(value)
    }

    fun resize(
        tableSize: SizeType,
        value: T,
    ) {
        while (buf.size > tableSize.toInt()) {
            buf.removeLast()
        }
        while (buf.size < tableSize.toInt()) {
            buf.add(value)
        }
    }

    fun resize(
        tableSize: SizeType,
        builder: () -> T,
    ) {
        while (buf.size > tableSize.toInt()) {
            buf.removeLast()
        }
        while (buf.size < tableSize.toInt()) {
            buf.add(builder())
        }
    }
}
