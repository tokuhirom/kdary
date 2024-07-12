package me.geso.kdary

fun <T> MutableList<T>.resize(
    tableSize: SizeType,
    value: T,
) {
    while (this.size > tableSize.toInt()) {
        this.removeLast()
    }
    while (this.size < tableSize.toInt()) {
        this.add(value)
    }
}

fun <T> MutableList<T>.resizeWithBlock(
    tableSize: SizeType,
    builder: () -> T,
) {
    while (this.size > tableSize.toInt()) {
        this.removeLast()
    }
    while (this.size < tableSize.toInt()) {
        this.add(builder())
    }
}
