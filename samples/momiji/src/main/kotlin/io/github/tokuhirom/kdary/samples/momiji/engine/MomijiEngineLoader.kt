package io.github.tokuhirom.kdary.samples.momiji.engine

import io.github.tokuhirom.kdary.loadKDary
import io.github.tokuhirom.kdary.samples.momiji.entity.WordEntry
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import java.io.File

class MomijiEngineLoader(
    dictDirectory: String,
) {
    private val dictCsv = "$dictDirectory/momiji.csv"
    private val dictKdary = "$dictDirectory/momiji.kdary"
    private val matrixFile = "$dictDirectory/matrix.def"
    private val charFile = "$dictDirectory/char.def"
    private val unkFile = "$dictDirectory/unk.def"

    fun load(): MomijiEngine {
        val kdary = loadKDary(dictKdary)
        val wordEntries = readDict(dictCsv.toPath())
        val connections = readMatrix()
        val charMap = parseCharDef(File(charFile))
        val unknownWords = readDict(unkFile.toPath())

        println(
            "Loaded dictionary: ${wordEntries.size} words, ${connections.size} connections," +
                " unknown words: ${unknownWords.size} types",
        )
        val wordEntryMap =
            wordEntries.groupBy {
                it.surface
            }
        val unknownWordsMap =
            unknownWords.groupBy {
                it.surface
            }

        val costManager =
            CostManager(
                wordEntryMap,
                connections.associate {
                    (it.leftContextId to it.rightContextId) to it.cost
                },
            )

        return MomijiEngine(kdary, wordEntryMap, costManager, charMap, unknownWordsMap)
    }

    private fun readDict(path: Path): List<WordEntry> {
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
