package io.github.tokuhirom.kdary.cli.mkkdary

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.boolean
import io.github.tokuhirom.kdary.KDary
import io.github.tokuhirom.kdary.saveKDary
import java.io.File

class MkkdaryApplication : CliktCommand() {
    private val sort by option(help = "Sort the keys").boolean().default(false)
    private val tab by option(help = "Use tab as a separator").boolean().default(false)

    private val input by argument()
    private val output by argument()

    override fun run() {
        val rows = sortIfRequired(loadFile(input))
        val keys = rows.map { it.first.toByteArray() }.toList()
        val values = rows.map { it.second }.toList()
        val progressBar = ProgressBar(rows.size)

        val kdary =
            KDary.build(
                keys,
                values,
            ) { current, _ ->
                progressBar.update(current)
            }

        saveKDary(kdary, output)
    }

    private fun sortIfRequired(rows: List<Pair<String, Int>>): List<Pair<String, Int>> =
        if (sort) {
            rows.sortedBy { it.first }
        } else {
            rows
        }

    private fun loadFile(input: String): List<Pair<String, Int>> {
        val lines = File(input).readLines()
        return lines
            .mapIndexedNotNull { index, line ->
                if (line.isEmpty()) {
                    null
                } else {
                    if (tab) {
                        val splitted: List<String> = line.split("\t", limit = 2)
                        check(splitted.size == 2) {
                            "Invalid line, doesn't contain the tab character: `$line` at line $index"
                        }
                        splitted[0] to splitted[1].toInt()
                    } else {
                        line to index
                    }
                }
            }.toList()
    }
}

fun main(args: Array<String>) {
    MkkdaryApplication().main(args)
}
