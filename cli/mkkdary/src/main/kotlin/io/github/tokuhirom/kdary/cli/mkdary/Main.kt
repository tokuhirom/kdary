package io.github.tokuhirom.kdary.cli.mkdary

import io.github.tokuhirom.kdary.KDary
import io.github.tokuhirom.kdary.saveKDary
import java.io.File

fun main(args: Array<String>) {
    if (args.size != 2) {
        println("Usage: mkdary <input-file> <output-file>")
        return
    }

    val inputFileName = args[0]
    val outputFileName = args[1]

    // Read the input file and split it by '\n'.
    // If a line contains '\t', it is formatted as "key\tvalue".
    // If a line doesn't contain '\t', it is formatted as "key".
    // If no value is available in the line, the value is set to the line number.
    val lines = File(inputFileName).readLines()
    val rows =
        lines
            .mapIndexedNotNull { index, line ->
                if (line.isEmpty()) {
                    null
                } else {
                    val splitted: List<String> = line.split("\t", limit = 2)
                    if (splitted.size == 1) {
                        splitted[0] to index
                    } else {
                        splitted[0] to splitted[1]
                    }
                }
            }.toList()
    val keys = rows.map { it.first.toByteArray() }.toList().toTypedArray()
    val values = rows.map { it.second }.toList().toTypedArray()
    val progressBar = ProgressBar()

    val kdary =
        KDary.build(
            keys,
            values,
        ) { current, total ->
            progressBar.update(current.toLong(), total.toLong())
        }

    saveKDary(kdary, outputFileName)
}
