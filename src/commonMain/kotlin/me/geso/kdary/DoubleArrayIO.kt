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
