package io.github.tokuhirom.kdary.internal

internal fun <T> MutableList<T>.resize(
    tableSize: Int,
    value: T,
) {
    when {
        this.size > tableSize -> {
            while (this.size > tableSize) {
                this.removeLast()
            }
        }
        this.size < tableSize -> {
            this.addAll(List(tableSize - this.size) { value })
        }
    }
}

internal fun <T> MutableList<T>.resizeWithBlock(
    tableSize: SizeType,
    builder: () -> T,
) {
    when {
        this.size > tableSize.toInt() -> {
            while (this.size > tableSize.toInt()) {
                this.removeLast()
            }
        }
        this.size < tableSize.toInt() -> {
            this.addAll(List(tableSize.toInt() - this.size) { builder() })
        }
    }
}
