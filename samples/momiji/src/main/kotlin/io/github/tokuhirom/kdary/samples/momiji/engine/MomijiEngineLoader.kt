package io.github.tokuhirom.kdary.samples.momiji.engine

import io.github.tokuhirom.kdary.loadKDary
import io.github.tokuhirom.kdary.samples.momiji.entity.WordEntry
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath

class MomijiEngineLoader(
    dictDirectory: String,
) {
    private val dictCsv = "$dictDirectory/momiji.csv"
    private val dictKdary = "$dictDirectory/momiji.kdary"
    private val matrixFile = "$dictDirectory/matrix.def"

    fun load(): MomijiEngine {
        val kdary = loadKDary(dictKdary)
        val wordEntries = readDict()
        val connections = readMatrix()

        println("Loaded dictionary: ${wordEntries.size} words, ${connections.size} connections")

        return MomijiEngine(kdary, wordEntries, connections)
    }

    private fun readDict(): List<WordEntry> {
        val path: Path = dictCsv.toPath()
        val fileSystem = FileSystem.SYSTEM
        println("READ DICT")
        return fileSystem.read(path) {
            var result = mutableListOf<WordEntry>()
            while (true) {
                val line = this.readUtf8Line() ?: break
                if (line.isBlank()) {
                    continue
                }
                result.add(WordEntry.parse(line))
            }
            result
        }
    }

    private fun readMatrix(): List<Connection> {
        // 最初の行に連接表のサイズ(前件サイズ, 後件サイズ)を書きます. その後は, 連接表の前件の文脈 ID, 後件の文脈IDと, それに対応するコストを書きます.
        val path: Path = matrixFile.toPath()
        val fileSystem = FileSystem.SYSTEM
        return fileSystem.read(path) {
            var result = mutableListOf<Connection>()
            this.readUtf8Line() // skip the first line
            while (true) {
                val line = this.readUtf8Line() ?: break
                if (line.isBlank()) {
                    continue
                }
                result.add(Connection.parse(line))
            }
            result
        }
    }
}
