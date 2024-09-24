package io.github.tokuhirom.kdary.cli.kdary

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.boolean
import io.github.tokuhirom.kdary.loadKDary
import io.github.tokuhirom.kdary.result.CommonPrefixSearchResult
import java.io.File

class KdaryApplication : CliktCommand() {
    private val tab by option(help = "Use tab as a separator").boolean().default(false)

    private val dictionary by argument()
    private val lexicon by argument()

    override fun run() {
        val dic = loadKDary(dictionary)

        if (lexicon == "-") {
            while (true) {
                val key = readlnOrNull()?.trim() ?: break
                val result: List<CommonPrefixSearchResult> = dic.commonPrefixSearch(key.toByteArray())
                println("Searching: $key, num: ${result.size}")
                result.forEach { item ->
                    println(" ${item.value} ${item.length}")
                }
            }
        } else {
            val keys =
                loadFile(lexicon)
                    .map {
                        it.first
                    }.toList()

            keys.forEach { key ->
                val result: List<CommonPrefixSearchResult> = dic.commonPrefixSearch(key.toByteArray())
                println("Searching: $key, num: ${result.size}")
                result.forEach { item ->
                    println(" ${item.value} ${item.length}")
                }
            }
        }
    }

    private fun loadFile(input: String): List<Pair<String, Any>> {
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
                        splitted[0] to splitted[1]
                    } else {
                        line to index
                    }
                }
            }.toList()
    }
}

fun main(args: Array<String>) {
    KdaryApplication().main(args)
}
