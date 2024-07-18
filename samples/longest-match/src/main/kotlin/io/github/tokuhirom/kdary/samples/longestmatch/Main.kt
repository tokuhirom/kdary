package io.github.tokuhirom.kdary.samples.longestmatch

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands

class LongestMatch : CliktCommand(name = "longest-match") {
    override fun run() = Unit
}

fun main(args: Array<String>) =
    LongestMatch()
        .subcommands(
            BuildDictCommand(),
            CleanCommand(),
            SearchCommand(),
        ).main(args)
