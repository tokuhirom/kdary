package me.geso.kdary

class AutoPool<T> {
    private var buf: MutableList<T?> = mutableListOf()
    private var sizeProp: Int = 0
    private var capacityProp: Int = 0

    operator fun get(id: Int): T = buf[id] ?: throw IndexOutOfBoundsException("Array index out of range: $id")

    operator fun set(
        id: Int,
        value: T,
    ) {
        buf[id] = value
    }

    fun isEmpty(): Boolean = sizeProp == 0

    fun size(): Int = sizeProp

    fun clear() {
        resize(0)
        buf.clear()
        sizeProp = 0
        capacityProp = 0
    }

    fun pushBack(value: T) {
        append(value)
    }

    fun popBack() {
        buf[--sizeProp] = null
    }

    fun append() {
        if (sizeProp == capacityProp) resizeBuffer(sizeProp + 1)
        buf.add(sizeProp, null as T?)
        sizeProp++
    }

    fun append(value: T) {
        if (sizeProp == capacityProp) resizeBuffer(sizeProp + 1)
        buf.add(sizeProp, value)
        sizeProp++
    }

    fun resize(size: Int) {
        while (sizeProp > size) {
            buf[--sizeProp] = null
        }
        if (size > capacityProp) {
            resizeBuffer(size)
        }
        while (sizeProp < size) {
            buf.add(sizeProp, null as T?)
            sizeProp++
        }
    }

    fun resize(
        size: Int,
        value: T,
    ) {
        while (sizeProp > size) {
            buf[--sizeProp] = null
        }
        if (size > capacityProp) {
            resizeBuffer(size)
        }
        while (sizeProp < size) {
            buf.add(sizeProp, value)
            sizeProp++
        }
    }

    fun reserve(size: Int) {
        if (size > capacityProp) {
            resizeBuffer(size)
        }
    }

    private fun resizeBuffer(size: Int) {
        var capacity = if (size >= capacityProp * 2) size else 1
        while (capacity < size) {
            capacity = capacity shl 1
        }
        val newBuf = MutableList<T?>(capacity) { null }
        for (i in 0 until sizeProp) {
            newBuf[i] = buf[i]
        }
        buf = newBuf
        capacityProp = capacity
    }
}
