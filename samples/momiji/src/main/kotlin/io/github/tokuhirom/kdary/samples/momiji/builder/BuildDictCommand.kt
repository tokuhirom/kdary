package io.github.tokuhirom.kdary.samples.momiji.builder

import com.github.ajalt.clikt.core.CliktCommand
import io.github.tokuhirom.kdary.KDary
import io.github.tokuhirom.kdary.samples.momiji.entity.WordEntry
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
    private val mecabDictDir = "mecab-ipadic-2.7.0-20070801"
    private val outputDir = "dict"
    private val outputCsv = "$outputDir/momiji.csv"
    private val outputKdary = "$outputDir/momiji.kdary"

    override fun run() {
        download()
        extract()
        val wordEntries = convertFiles()
        buildKdary(wordEntries)
        copyFiles()
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
        File(mecabDictDir).mkdir()
        val processBuilder = ProcessBuilder("tar", "-xzvf", tarball)
        val process = processBuilder.start()
        process.waitFor()
        println("Extracted to $mecabDictDir")
    }

    private fun convertFiles(): List<WordEntry> {
        val csvFiles =
            File(mecabDictDir).listFiles { _, name ->
                name.endsWith(".csv")
            }
        val eucJpCharset = Charset.forName("EUC-JP")
        val lines =
            csvFiles!!
                .flatMap { it.readLines(eucJpCharset) }
                .map { WordEntry.parse(it) }
                .sortedBy { it.surface }

        File(outputCsv).writeText(lines.joinToString("\n"))
        println("Converted to $outputCsv")

        return lines
    }

    private fun buildKdary(wordEntries: List<WordEntry>) {
        val kdary = KDary.build(wordEntries.map { it.surface.toByteArray(Charsets.UTF_8) })
        saveKDary(kdary, outputKdary)
        println("Built KDary dictionary at $outputKdary")
    }

    private fun copyFiles() {
        // mecabDictDir の中のファイルを dict ディレクトリにコピー
        listOf("matrix.def", "char.def", "unk.def").forEach { file ->
            val sourceFile = File(mecabDictDir, file)
            val targetFile = File(outputDir, file)
            copyFileWithEncoding(sourceFile, targetFile, "EUC-JP", "UTF-8")
            println("Copied $file to $outputDir")
        }
    }

    private fun copyFileWithEncoding(
        source: File,
        target: File,
        sourceEncoding: String,
        targetEncoding: String,
    ) {
        val sourceCharset = Charset.forName(sourceEncoding)
        val targetCharset = Charset.forName(targetEncoding)

        source.bufferedReader(sourceCharset).use { reader ->
            target.bufferedWriter(targetCharset).use { writer ->
                reader.lineSequence().forEach { line ->
                    writer.write(line)
                    writer.newLine()
                }
            }
        }
    }
}
