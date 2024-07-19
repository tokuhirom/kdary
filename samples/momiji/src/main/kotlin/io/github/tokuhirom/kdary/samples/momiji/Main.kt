package io.github.tokuhirom.kdary.samples.momiji

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import io.github.tokuhirom.kdary.samples.momiji.builder.BuildDictCommand
import io.github.tokuhirom.kdary.samples.momiji.engine.EngineCommand

class LongestMatch : CliktCommand(name = "longest-match") {
    override fun run() = Unit
}

fun main(args: Array<String>) =
    LongestMatch()
        .subcommands(
            BuildDictCommand(),
            EngineCommand(),
        ).main(args)
