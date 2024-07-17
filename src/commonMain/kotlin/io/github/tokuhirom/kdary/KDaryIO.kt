package io.github.tokuhirom.kdary

import io.github.tokuhirom.kdary.KDary.Companion.UNIT_SIZE
import io.github.tokuhirom.kdary.internal.DoubleArrayUnit
import okio.FileSystem
import okio.Path.Companion.toPath

class DoubleArrayIOException(
    message: String,
) : Exception(message)

/**
 * Saves the double array into the specified file.
 *
 * @param fileName The name of the file to save to.
 * @throws IllegalStateException If the array is empty.
 */
fun saveKDary(
    kdary: KDary,
    fileName: String,
) {
    val file = fileName.toPath()
    getFileSystem().write(file) {
        kdary.array.forEach { unit ->
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
fun loadKDary(fileName: String): KDary {
    val fileSystem = getFileSystem()
    val file = fileName.toPath()
    if (!fileSystem.exists(file)) {
        throw DoubleArrayIOException("File not found: $fileName")
    }

    return fileSystem.read(file) {
        val source = this
        val metadata = fileSystem.metadata(file)
        val actualSize =
            metadata.size?.toULong()
                ?: throw DoubleArrayIOException("Cannot get the size of the file: $fileName")

        val unitSize = UNIT_SIZE.toSizeType()
        val numUnits = actualSize / unitSize
        if (numUnits < 256uL || numUnits % 256u != 0uL) {
            throw DoubleArrayIOException("numUnits must be 256 or multiple of 256: $numUnits")
        }

        val headerBuffer = ByteArray(256 * UNIT_SIZE)
        val readSize = source.read(headerBuffer, 0, 256 * UNIT_SIZE)
        if (readSize != 256 * UNIT_SIZE) {
            throw DoubleArrayIOException("Failed to read the header of KDary file from $fileName: $readSize")
        }

        val units =
            (0 until 256)
                .map { readUIntLeFromBuffer(it, headerBuffer) }
                .toTypedArray()

        if (units[0].label() != 0u ||
            units[0].hasLeaf() ||
            units[0].offset() == 0u ||
            units[0].offset() >= 512u
        ) {
            throw DoubleArrayIOException("Broken kdary header: $fileName")
        }

        for (i in 1 until 256) {
            if (units[i].label() <= 0xFF.toUInt() && units[i].offset() >= numUnits.toUInt()) {
                throw DoubleArrayIOException("Invalid file format(bad unit): $fileName")
            }
        }

        val buf = ByteArray((numUnits - 256u).toInt() * UNIT_SIZE)
        source.readFully(buf)

        val doubleArrayUnits: Array<DoubleArrayUnit> = Array(numUnits.toInt()) { DoubleArrayUnit(0u) }

        for (i in units.indices) {
            doubleArrayUnits[i] = units[i]
        }

        for (i in 0 until (numUnits - 256u).toInt()) {
            doubleArrayUnits[i + 256] = readUIntLeFromBuffer(i, buf)
        }

        KDary(doubleArrayUnits)
    }
}

internal expect fun getFileSystem(): FileSystem

private fun readUIntLeFromBuffer(
    i: Int,
    buf: ByteArray,
): DoubleArrayUnit {
    val offset = i * UNIT_SIZE.toInt()
    val value = (
        (buf[offset].toUInt() and 0xFFU) or
            ((buf[offset + 1].toUInt() and 0xFFU) shl 8) or
            ((buf[offset + 2].toUInt() and 0xFFU) shl 16) or
            ((buf[offset + 3].toUInt() and 0xFFU) shl 24)
    )
    return DoubleArrayUnit(value)
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
