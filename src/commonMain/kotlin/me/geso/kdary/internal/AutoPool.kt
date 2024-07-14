package me.geso.kdary.internal

import me.geso.kdary.SizeType

fun <T> MutableList<T>.resize(
    tableSize: SizeType,
    value: T,
) {
    when {
        this.size > tableSize.toInt() -> {
            while (this.size > tableSize.toInt()) {
                this.removeLast()
            }
        }
        this.size < tableSize.toInt() -> {
            this.addAll(List(tableSize.toInt() - this.size) { value })
        }
    }
}

fun <T> MutableList<T>.resizeWithBlock(
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
