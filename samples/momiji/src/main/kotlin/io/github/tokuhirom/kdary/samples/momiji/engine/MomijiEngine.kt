package io.github.tokuhirom.kdary.samples.momiji.engine

import io.github.tokuhirom.kdary.KDary
import io.github.tokuhirom.kdary.samples.momiji.entity.WordEntry

data class WordResult(
    val surface: String,
    val cost: Int, // 単語コスト
    val annotations: List<String>, // その他の情報
)

data class MomijiEngine(
    val kdary: KDary,
    val wordEntries: List<WordEntry>,
    val connections: List<Connection>,
) {
    fun analysis(src: String): List<WordResult> {
        val lattice = buildLattice(src)
        dumpLattice(lattice)

        // ラティス構造を元に最適経路を探索
        return findOptimalPath(lattice)
    }

    private fun dumpLattice(lattice: List<List<Node>>) {
        for (i in lattice.indices) {
            println("Lattice[$i]: ${lattice[i].joinToString { it.wordEntry.surface }}")
        }
    }

    private fun buildLattice(src: String): List<List<Node>> {
        val lattice = mutableListOf<MutableList<Node>>()

        for (i in src.indices) {
            val nodes = mutableListOf<Node>()
            val results = kdary.commonPrefixSearch(src.substring(i).toByteArray(Charsets.UTF_8))

            if (results.isEmpty()) {
                val c = src.substring(i, i + 1)
                // 未知語として1文字だけ登録。将来的には改善の余地あり。
                val entry =
                    WordEntry(
                        surface = c,
                        leftId = 0,
                        rightId = 0,
                        cost = 1000,
                        annotations = emptyList(),
                    )
                nodes.add(Node(entry, i, i + c.encodeToByteArray().size))
            } else {
                for (result in results) {
                    val entry = wordEntries[result.value]
                    nodes.add(Node(entry, i, i + result.length))
                }
            }

            lattice.add(nodes)
        }

        return lattice
    }

    private fun findOptimalPath(lattice: List<List<Node>>): List<WordResult> {
        val dp = mutableListOf<MutableList<Path>>()

        for (i in lattice.indices) {
            dp.add(mutableListOf())
            for (node in lattice[i]) {
                if (i == 0) {
                    // 最初の位置では、直接コストを追加
                    dp[i].add(Path(node, node.wordEntry.cost))
                } else {
                    // 前の位置から最小コストの経路を見つける
                    val bestPath =
                        dp[i - 1].minByOrNull { path ->
                            path.cost + getConnectionCost(path.node.wordEntry.rightId, node.wordEntry.leftId)
                        }
                    if (bestPath != null) {
                        val newPath =
                            bestPath.copy(
                                node = node,
                                cost =
                                    bestPath.cost + node.wordEntry.cost +
                                        getConnectionCost(bestPath.node.wordEntry.rightId, node.wordEntry.leftId),
                                previous = bestPath,
                            )
                        dp[i].add(newPath)
                    }
                }
            }
        }
        // 最後の位置から最小コストの経路を再構築
        val optimalPath = dp.last().minByOrNull { it.cost } ?: throw IllegalStateException("No path found")
        return reconstructPath(optimalPath)
    }

    private fun getConnectionCost(
        leftId: Int,
        rightId: Int,
    ): Int {
        val connection = connections.find { it.leftContextId == leftId && it.rightContextId == rightId }
        return connection?.cost?.toInt() ?: Int.MAX_VALUE
    }

    private fun reconstructPath(path: Path): List<WordResult> {
        val result = mutableListOf<WordResult>()
        var currentPath: Path? = path
        while (currentPath != null) {
            result.add(
                0,
                WordResult(
                    surface = currentPath.node.wordEntry.surface,
                    cost = currentPath.node.wordEntry.cost,
                    annotations = currentPath.node.wordEntry.annotations,
                ),
            )
            currentPath = currentPath.previous
        }
        return result
    }

    data class Node(
        val wordEntry: WordEntry,
        val start: Int,
        val end: Int,
    )

    data class Path(
        val node: Node,
        val cost: Int,
        val previous: Path? = null,
    )
}
