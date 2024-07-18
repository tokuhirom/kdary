package io.github.tokuhirom.kdary.samples.longestmatch

import com.github.ajalt.clikt.core.CliktCommand
import io.github.tokuhirom.kdary.KDary
import io.github.tokuhirom.kdary.saveKDary
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.jvm.javaio.copyTo
import kotlinx.coroutines.runBlocking
import java.io.File
import java.nio.charset.Charset

class BuildDictCommand : CliktCommand() {
    private val url = "https://drive.google.com/uc?export=download&id=0B4y35FiV1wh7MWVlSDBCSXZMTXM"
    private val tarball = "mecab-ipadic.tar.gz"
    private val outputDir = "mecab-ipadic-2.7.0-20070801"
    private val outputCsv = "$outputDir/Noun.utf8.csv"
    private val outputKdary = "noun.kdary"

    override fun run() {
        download()
        extract()
        convertFiles()
        buildKdary()
    }

    private fun download(): String =
        runBlocking {
            val client = HttpClient()
            val response = client.get(url)
            val file = File(tarball)
            response.bodyAsChannel().copyTo(file.outputStream())
            println("Downloaded $tarball")
            tarball
        }

    private fun extract() {
        File(outputDir).mkdir()
        val processBuilder = ProcessBuilder("tar", "-xzvf", tarball)
        val process = processBuilder.start()
        process.waitFor()
        println("Extracted to $outputDir")
    }

    private fun convertFiles() {
        val csvFiles =
            File(outputDir).listFiles { _, name ->
                name.startsWith("Noun") && name.endsWith(".csv")
            }
        val eucJpCharset = Charset.forName("EUC-JP")
        val lines =
            csvFiles!!
                .flatMap { it.readLines(eucJpCharset) }
                .map { it.split(",")[0] }
                .distinct()
                .sorted()

        File(outputCsv).writeText(lines.joinToString("\n"))
        println("Converted to $outputCsv")
    }

    private fun buildKdary() {
        val lines = File(outputCsv).readLines(Charsets.UTF_8).map { it.toByteArray(Charsets.UTF_8) }
        val kdary = KDary.build(lines)
        saveKDary(kdary, outputKdary)
        println("Built KDary dictionary at $outputKdary")
    }
}
