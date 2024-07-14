package me.geso.kdary

import okio.IOException

class DoubleArrayIOException(
    message: String,
) : Exception(message)

/**
 * Reads an array of units from the specified file.
 *
 * @param fileName The name of the file to read.
 * @return A DoubleArray containing the read units.
 * @throws IOException If the file is not found or invalid.
 */
expect fun loadDoubleArray(fileName: String): DoubleArray

/**
 * Saves the double array into the specified file.
 *
 * @param fileName The name of the file to save to.
 * @throws IllegalStateException If the array is empty.
 */
expect fun saveDoubleArray(
    doubleArray: DoubleArray,
    fileName: String,
)

/**
 * Reads an unsigned 32-bit integer in little-endian format from the given source.
 *
 * @param source The BufferedSource to read from.
 * @return The unsigned 32-bit integer read from the source.
 */
internal fun readUIntLe(source: okio.BufferedSource): UInt {
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
internal fun writeUIntLe(
    sink: okio.BufferedSink,
    value: UInt,
) {
    sink.writeByte((value and 0xFFU).toInt())
    sink.writeByte(((value shr 8) and 0xFFU).toInt())
    sink.writeByte(((value shr 16) and 0xFFU).toInt())
    sink.writeByte(((value shr 24) and 0xFFU).toInt())
}
