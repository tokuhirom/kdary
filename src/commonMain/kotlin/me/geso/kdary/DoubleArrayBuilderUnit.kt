package me.geso.kdary

/**
 * Unit of double-array builder.
 */
class DoubleArrayBuilderUnit {
    private var unit: UInt = 0u

    fun setHasLeaf(hasLeaf: Boolean) {
        unit =
            if (hasLeaf) {
                unit or (1u shl 8)
            } else {
                unit and (1u shl 8).inv()
            }
    }

    fun setValue(value: Int) {
        unit = value.toUInt() or (1u shl 31)
    }

    fun setLabel(label: UByte) {
        unit = (unit and 0xFFFFFF00u) or label.toUInt()
    }

    fun setOffset(offset: UInt) {
        if (offset >= (1u shl 29)) {
            throw IllegalArgumentException("failed to modify unit: too large offset")
        }
        unit = unit and ((1u shl 31) or (1u shl 8) or 0xFFu)
        unit =
            if (offset < (1u shl 21)) {
                unit or (offset shl 10)
            } else {
                unit or (offset shl 2) or (1u shl 9)
            }
    }
}
