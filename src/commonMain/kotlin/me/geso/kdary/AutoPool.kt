package me.geso.kdary

fun <T> MutableList<T>.size(): SizeType = this.size.toSizeType()

fun <T> MutableList<T>.pushBack(value: T) = this.append(value)

fun <T> MutableList<T>.popBack() = this.removeLast()

fun <T> MutableList<T>.append(value: T) = this.add(value)

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
