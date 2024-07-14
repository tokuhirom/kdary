package me.geso.kdary

import me.geso.kdary.internal.DoubleArrayUnit
import okio.IOException
import okio.buffer
import okio.sink
import okio.source
import java.io.File

/**
 * Reads an unsigned 32-bit integer in little-endian format from the given source.
 *
 * @param source The BufferedSource to read from.
 * @return The unsigned 32-bit integer read from the source.
 */
private fun readUIntLe(source: okio.BufferedSource): UInt {
    val byte1 = source.readByte().toUInt() and 0xFFU
    val byte2 = source.readByte().toUInt() and 0xFFU
    val byte3 = source.readByte().toUInt() and 0xFFU
    val byte4 = source.readByte().toUInt() and 0xFFU
    return (byte1 or (byte2 shl 8) or (byte3 shl 16) or (byte4 shl 24))
}

/**
 * Writes an unsigned 32-bit integer in little-endian format to the given sink.
 *
 * @param sink The BufferedSink to write to.
 * @param value The unsigned 32-bit integer to write.
 */
private fun writeUIntLe(
    sink: okio.BufferedSink,
    value: UInt,
) {
    sink.writeByte((value and 0xFFU).toInt())
    sink.writeByte(((value shr 8) and 0xFFU).toInt())
    sink.writeByte(((value shr 16) and 0xFFU).toInt())
    sink.writeByte(((value shr 24) and 0xFFU).toInt())
}

/**
 * Reads an array of units from the specified file.
 *
 * @param fileName The name of the file to read.
 * @return A DoubleArray containing the read units.
 * @throws IOException If the file is not found or invalid.
 */
fun loadDoubleArray(fileName: String): DoubleArray {
    val file = File(fileName)
    if (!file.exists()) {
        throw IOException("File not found: $fileName")
    }

    return file.source().buffer().use { source ->
        val actualSize = file.length().toULong()

        val unitSize = DoubleArray.unitSize()
        val numUnits = actualSize / unitSize
        if (numUnits < 256uL || numUnits % 256u != 0uL) {
            throw IOException("numUnits must be 256 or multiple of 256: $numUnits")
        }

        val units = Array(256) { DoubleArrayUnit(0u) }
        for (i in units.indices) {
            units[i] = DoubleArrayUnit(readUIntLe(source))
        }

        if (units[0].label().toInt().toChar() != '\u0000' ||
            units[0].hasLeaf() ||
            units[0].offset() == 0u ||
            units[0].offset() >= 512u
        ) {
            throw IOException("Invalid file format")
        }

        for (i in 1 until 256) {
            if (units[i].label() <= 0xFF.toUInt() && units[i].offset() >= numUnits.toUInt()) {
                throw IOException("Invalid file format(bad unit)")
            }
        }

        val buf = ByteArray((numUnits - 256u).toInt() * DoubleArray.unitSize().toInt())
        source.readFully(buf)

        val doubleArrayUnits: Array<DoubleArrayUnit> = Array(numUnits.toInt()) { DoubleArrayUnit(0u) }

        for (i in units.indices) {
            doubleArrayUnits[i] = units[i]
        }

        for (i in 0 until (numUnits - 256u).toInt()) {
            val offset = i * DoubleArray.unitSize().toInt()
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

/**
 * Saves the double array into the specified file.
 *
 * @param fileName The name of the file to save to.
 * @throws IllegalStateException If the array is empty.
 */
fun saveDoubleArray(
    doubleArray: DoubleArray,
    fileName: String,
) {
    check(doubleArray.array().size.toSizeType() != 0uL) {
        "You can't save empty array"
    }

    val file = File(fileName)
    file.sink().buffer().use { sink ->
        doubleArray.array().forEach { unit ->
            writeUIntLe(sink, unit.unit)
        }
    }
}
