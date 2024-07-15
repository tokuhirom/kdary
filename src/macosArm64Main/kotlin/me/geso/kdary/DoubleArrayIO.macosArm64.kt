package me.geso.kdary

import me.geso.kdary.DoubleArray.Companion.unitSize
import me.geso.kdary.internal.DoubleArrayUnit
import okio.FileSystem
import okio.Path.Companion.toPath

/**
 * Saves the double array into the specified file.
 *
 * @param fileName The name of the file to save to.
 * @throws IllegalStateException If the array is empty.
 */
actual fun saveDoubleArray(
    doubleArray: DoubleArray,
    fileName: String,
) {
    check(doubleArray.array().size.toSizeType() != 0uL) {
        "You can't save empty array"
    }

    val file = fileName.toPath()
    FileSystem.SYSTEM.write(file) {
        doubleArray.array().forEach { unit ->
            writeUIntLe(this, unit.unit)
        }
    }
}

/**
 * Reads an array of units from the specified file.
 *
 * @param fileName The name of the file to read.
 * @return A DoubleArray containing the read units.
 * @throws DoubleArrayIOException If the file is not found or invalid.
 */
actual fun loadDoubleArray(fileName: String): DoubleArray {
    val file = fileName.toPath()
    if (!FileSystem.SYSTEM.exists(file)) {
        throw DoubleArrayIOException("File not found: $fileName")
    }

    return FileSystem.SYSTEM.read(file) {
        val actualSize =
            FileSystem.SYSTEM
                .metadata(file)
                .size
                ?.toULong()
        check(actualSize != null)

        val unitSize = unitSize()
        val numUnits = actualSize / unitSize
        if (numUnits < 256uL || numUnits % 256u != 0uL) {
            throw DoubleArrayIOException("numUnits must be 256 or multiple of 256: $numUnits")
        }

        val units = Array(256) { DoubleArrayUnit(0u) }
        for (i in units.indices) {
            units[i] = DoubleArrayUnit(readUIntLe(this))
        }

        if (units[0].label().toInt().toChar() != '\u0000' ||
            units[0].hasLeaf() ||
            units[0].offset() == 0u ||
            units[0].offset() >= 512u
        ) {
            throw DoubleArrayIOException("Invalid file format")
        }

        for (i in 1 until 256) {
            if (units[i].label() <= 0xFF.toUInt() && units[i].offset() >= numUnits.toUInt()) {
                throw DoubleArrayIOException("Invalid file format(bad unit)")
            }
        }

        val buf = ByteArray((numUnits - 256u).toInt() * unitSize().toInt())
        this.readFully(buf)

        val doubleArrayUnits: Array<DoubleArrayUnit> = Array(numUnits.toInt()) { DoubleArrayUnit(0u) }

        for (i in units.indices) {
            doubleArrayUnits[i] = units[i]
        }

        for (i in 0 until (numUnits - 256u).toInt()) {
            val offset = i * unitSize().toInt()
            val value = (
                (buf[offset].toUInt() and 0xFFU) or
                    ((buf[offset + 1].toUInt() and 0xFFU) shl 8) or
                    ((buf[offset + 2].toUInt() and 0xFFU) shl 16) or
                    ((buf[offset + 3].toUInt() and 0xFFU) shl 24)
            )
            doubleArrayUnits[i + 256] = DoubleArrayUnit(value)
        }

        DoubleArray(doubleArrayUnits)
    }
}
