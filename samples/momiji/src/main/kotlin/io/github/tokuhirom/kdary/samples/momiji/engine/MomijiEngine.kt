package io.github.tokuhirom.kdary.samples.momiji.engine

import io.github.tokuhirom.kdary.KDary
import io.github.tokuhirom.kdary.samples.momiji.entity.WordEntry

data class MomijiEngine(
    private val kdary: KDary,
    private val wordEntries: Map<String, List<WordEntry>>,
    internal val costManager: CostManager,
    private val charMap: CharMap,
    private val unknownWords: List<WordEntry>,
) {
    fun analysis(src: String): List<Node> {
        val lattice = buildLattice(src)
        return lattice.viterbi()
    }

    private fun buildLattice(src: String): Lattice {
        val lattice = Lattice(src, costManager)

        for (i in src.indices) {
            val bytes = src.substring(i).toByteArray(Charsets.UTF_8)
            val results = kdary.commonPrefixSearch(bytes)

            var hasSingleWord = false
            results.forEach { word ->
                val s = bytes.decodeToString(0, word.length)
                wordEntries[s]?.forEach { wordEntry ->
                    lattice.insert(i, i + s.length, wordEntry)
                }
                if (s.length == 1) {
                    hasSingleWord = true
                }
            }
            if (!hasSingleWord) {
                // 1文字の単語がない場合は、1文字の未知語を追加する。
                lattice.insert(i, i + 1, null)
            }
        }

        return lattice
    }
}
