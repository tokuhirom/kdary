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

    fun reserve(numUnits: SizeType) {
        // do nothing in kotlin
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

  template <typename T>
void AutoPool<T>::resize_buf(std::size_t size) {
  std::size_t capacity;
  if (size >= capacity_ * 2) {
    capacity = size;
  } else {
    capacity = 1;
    while (capacity < size) {
      capacity <<= 1;
    }
  }

  AutoArray<char> buf;
  try {
    buf.reset(new char[sizeof(T) * capacity]);
  } catch (const std::bad_alloc &) {
    DARTS_THROW("failed to resize pool: std::bad_alloc");
  }

  if (size_ > 0) {
    T *src = reinterpret_cast<T *>(&buf_[0]);
    T *dest = reinterpret_cast<T *>(&buf[0]);
    for (std::size_t i = 0; i < size_; ++i) {
      new(&dest[i]) T(src[i]);
      src[i].~T();
    }
  }

  buf_.swap(&buf);
  capacity_ = capacity;
}
     */
}
