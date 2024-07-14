package me.geso.kdary

import me.geso.kdary.DoubleArray.Companion.unitSize
import me.geso.kdary.internal.DoubleArrayUnit
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get
import org.khronos.webgl.set

private external fun require(module: String): dynamic

private val fs = require("fs")

/**
 * Reads an unsigned 32-bit integer in little-endian format from the given buffer.
 *
 * @param buffer The Buffer to read from.
 * @param offset The offset to start reading from.
 * @return The unsigned 32-bit integer read from the buffer.
 */
private fun readUIntLe(
    buffer: Uint8Array,
    offset: Int,
): UInt {
    val byte1 = buffer[offset].toUInt() and 0xFFU
    val byte2 = buffer[offset + 1].toUInt() and 0xFFU
    val byte3 = buffer[offset + 2].toUInt() and 0xFFU
    val byte4 = buffer[offset + 3].toUInt() and 0xFFU
    return (byte1 or (byte2 shl 8) or (byte3 shl 16) or (byte4 shl 24))
}

/**
 * Writes an unsigned 32-bit integer in little-endian format to the given buffer.
 *
 * @param buffer The Buffer to write to.
 * @param offset The offset to start writing from.
 * @param value The unsigned 32-bit integer to write.
 */
private fun writeUIntLe(
    buffer: Uint8Array,
    offset: Int,
    value: UInt,
) {
    buffer[offset] = (value and 0xFFU).toByte()
    buffer[offset + 1] = ((value shr 8) and 0xFFU).toByte()
    buffer[offset + 2] = ((value shr 16) and 0xFFU).toByte()
    buffer[offset + 3] = ((value shr 24) and 0xFFU).toByte()
}

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

    val buffer = Uint8Array(doubleArray.array().size * 4)
    doubleArray.array().forEachIndexed { index, unit ->
        writeUIntLe(buffer, index * 4, unit.unit)
    }

    fs.writeFileSync(fileName, buffer)
}

/**
 * Reads an array of units from the specified file.
 *
 * @param fileName The name of the file to read.
 * @return A DoubleArray containing the read units.
 * @throws IOException If the file is not found or invalid.
 */
actual fun loadDoubleArray(fileName: String): DoubleArray {
    if (!fs.existsSync(fileName) as Boolean) {
        throw DoubleArrayIOException("File not found: $fileName")
    }

    val buffer = fs.readFileSync(fileName) as Uint8Array
    val actualSize = buffer.length.toULong()

    val unitSize = unitSize()
    val numUnits = actualSize / unitSize
    if (numUnits < 256uL || numUnits % 256u != 0uL) {
        throw DoubleArrayIOException("numUnits must be 256 or multiple of 256: $numUnits")
    }

    val units = Array(256) { DoubleArrayUnit(0u) }
    for (i in units.indices) {
        units[i] = DoubleArrayUnit(readUIntLe(buffer, i * 4))
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

    val doubleArrayUnits: Array<DoubleArrayUnit> = Array(numUnits.toInt()) { DoubleArrayUnit(0u) }

    for (i in units.indices) {
        doubleArrayUnits[i] = units[i]
    }

    for (i in 0 until (numUnits - 256u).toInt() as Int) {
        val offset = (i + 256) * 4
        val value = (
            (buffer[offset].toUInt() and 0xFFU) or
                ((buffer[offset + 1].toUInt() and 0xFFU) shl 8) or
                ((buffer[offset + 2].toUInt() and 0xFFU) shl 16) or
                ((buffer[offset + 3].toUInt() and 0xFFU) shl 24)
        )
        doubleArrayUnits[i + 256] = DoubleArrayUnit(value)
    }

    return DoubleArray(doubleArrayUnits)
}
