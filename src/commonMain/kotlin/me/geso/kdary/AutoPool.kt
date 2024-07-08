package me.geso.kdary

/**
 * Memory management of resizable array.
 */
class AutoPool<T> {
    // This implementation is based on MutableList.
    private var buf: MutableList<T> = mutableListOf()

    operator fun get(id: Int): T = buf[id] ?: throw IndexOutOfBoundsException("Array index out of range: $id")

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

    // append, resize, reserve を実装する。

    /*
      void append() {
    if (size_ == capacity_)
      resize_buf(size_ + 1);
    new(&(*this)[size_++]) T;
  }
  void append(const T &value) {
    if (size_ == capacity_)
      resize_buf(size_ + 1);
    new(&(*this)[size_++]) T(value);
  }
     */

    // XXX 引数無しの append は使い道なさそうなので実装せず。
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

    /*

  void resize(std::size_t size) {
    while (size_ > size) {
      (*this)[--size_].~T();
    }
    if (size > capacity_) {
      resize_buf(size);
    }
    while (size_ < size) {
      new(&(*this)[size_++]) T;
    }
  }
  void resize(std::size_t size, const T &value) {
    while (size_ > size) {
      (*this)[--size_].~T();
    }
    if (size > capacity_) {
      resize_buf(size);
    }
    while (size_ < size) {
      new(&(*this)[size_++]) T(value);
    }
  }

  void reserve(std::size_t size) {
    if (size > capacity_) {
      resize_buf(size);
    }
  }
     */
}
