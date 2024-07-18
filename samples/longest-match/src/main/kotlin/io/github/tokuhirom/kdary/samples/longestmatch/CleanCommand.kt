package io.github.tokuhirom.kdary.samples.longestmatch

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import java.io.File

class CleanCommand : CliktCommand() {
    private val filesToDelete by option(help = "Files and directories to delete")
        .default("noun.kdary mecab-ipadic.tar.gz mecab-ipadic mecab-ipadic-2.7.0-20070801")

    override fun run() {
        filesToDelete.split(" ").forEach {
            File(it).deleteRecursively()
        }
        println("Cleaned up files")
    }
}
