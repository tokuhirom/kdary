package io.github.tokuhirom.kdary.samples.momiji.engine

import io.github.tokuhirom.kdary.KDary
import io.github.tokuhirom.kdary.samples.momiji.engine.MomijiEngine.Node
import io.github.tokuhirom.kdary.samples.momiji.entity.WordEntry
import java.io.File

data class CostManager(
    private val wordEntries: Map<String, List<WordEntry>>,
    // Map<Pair<leftContextId, rightContextId>, cost>
    private val connections: Map<Pair<Int, Int>, Short>,
) {
    /**
     * 生起コスト
     */
    fun getEmissionCost(node: Node): Int = node.wordEntry?.cost ?: 0

    /**
     * 連接コスト
     */
    fun getTransitionCost(
        left: Node,
        right: Node,
    ): Short {
        val leftRightId =
            if (left.surface == "__BOS__") {
                0
            } else {
                val lWordEntry = left.wordEntry ?: return 0
                lWordEntry.rightId
            }
        val rightLeftId =
            if (right.surface == "__EOS__") {
                0
            } else {
                val rWordEntry = right.wordEntry ?: return 0
                rWordEntry.leftId
            }

        return connections.getOrDefault(leftRightId to rightLeftId, 0)
    }
}

data class MomijiEngine(
    private val kdary: KDary,
    private val wordEntries: Map<String, List<WordEntry>>,
    private val costManager: CostManager,
) {
    fun analysis(src: String): List<Node> {
        val lattice = buildLattice(src)

        lattice.dump()

        lattice.exportToDot("/tmp/foo.dot")

        // ラティス構造を元に最適経路を探索
        val result = lattice.viterbi()
        result.forEachIndexed { index, node ->
            val transitionCost =
                node.minPrev?.let { prev ->
                    costManager.getTransitionCost(prev, node)
                } ?: 0
            println(" $index ${node.surface} 連接=$transitionCost minCost=${node.minCost} ${node.wordEntry?.cost}")
        }
        return result
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

    /**
     * Every index should be the character index.
     */
    class Lattice(
        private val sentence: String,
        private val costManager: CostManager,
    ) {
        val beginNodes: MutableList<MutableList<Node>> = mutableListOf()
        val endNodes: MutableList<MutableList<Node>> = mutableListOf()

        init {
            // +1 for EOS node
            for (i in 0 until sentence.length + 1) {
                beginNodes.add(mutableListOf())
                endNodes.add(mutableListOf())
            }

            // register BOS node
            val bos = Node(surface = "__BOS__", length = 0, wordEntry = null)
            endNodes[0].add(bos)

            // register EOS node
            val eos = Node(surface = "__EOS__", length = 0, wordEntry = null)
            beginNodes[sentence.length].add(eos)
        }

        fun insert(
            begin: Int,
            end: Int,
            wordEntry: WordEntry? = null,
        ): Node {
            val node =
                Node(
                    surface = sentence.substring(begin, end),
                    length = end - begin,
                    wordEntry = wordEntry,
                )
            beginNodes[begin].add(node)
            endNodes[end].add(node)
            return node
        }

        fun dump() {
            dump("begin", beginNodes)
            dump("end", endNodes)
        }

        private fun dump(
            type: String,
            lattice: List<List<Node>>,
        ) {
            for (i in lattice.indices) {
                println(
                    "$type[$i]: ${lattice[i].joinToString {
                        it.surface + " (cost=${it.wordEntry?.cost}, " + if (it.minCost != Int.MAX_VALUE) "(${it.minCost})" else ""
                    }}",
                )
            }
        }

        // 微旅系列を返す
        fun viterbi(): List<Node> {
            val bos = endNodes[0][0]
            bos.minPrev = null
            bos.minCost = 0

            for (i in 0 until sentence.length + 1) {
                for (rnode in beginNodes[i]) {
                    rnode.minPrev = null
                    rnode.minCost = Int.MAX_VALUE

                    for (lnode in endNodes[i]) {
                        val cost =
                            lnode.minCost +
                                costManager.getTransitionCost(lnode, rnode) + // transition cost
                                costManager.getEmissionCost(rnode) // emission cost
                        if (cost < rnode.minCost) {
                            rnode.minCost = cost
                            rnode.minPrev = lnode
                        }
                    }
                }
            }

            dump()

            // minPrev を EOS から BOS までたどり、最後に反転する
            val results = mutableListOf<Node>()
            val eos = beginNodes[sentence.length][0]
            var node: Node? = eos
            while (node != null) {
                results.add(node)
                node = node.minPrev
            }
            return results.reversed()
        }

        // DOT形式でエクスポートするメソッド
        fun exportToDot(filePath: String) {
            val file = File(filePath)
            file.printWriter().use { out ->
                out.println("digraph lattice {")
                out.println("rankdir=LR;")

                val bos = endNodes[0][0]
                out.println("node_${bos.hashCode().toUInt()} [label=\"__BOS__\"];")

                // ノードのエクスポート
                for (i in beginNodes.indices) {
                    for (node in beginNodes[i]) {
                        val label = node.surface.replace("\"", "\\\"") + node.wordEntry?.annotations?.joinToString(",")
                        out.println("node_${node.hashCode().toUInt()} [label=\"$label (${node.wordEntry?.cost ?: "?"})\"];")
                    }
                }

                // エッジのエクスポート
                for (i in beginNodes.indices) {
                    out.println("/* $i */")
                    for (rnode in beginNodes[i]) {
                        out.println("  /* rnode:${rnode.surface} */")
                        for (lnode in endNodes[i]) {
                            val transitionCost = costManager.getTransitionCost(lnode, rnode)
                            val emissionCost = costManager.getEmissionCost(rnode)
                            val totalCost = transitionCost + emissionCost
                            out.println(
                                "    /* lnode:${lnode.surface}(rid=${lnode.wordEntry?.rightId}) rnode:${rnode.surface}(lid=${rnode.wordEntry?.leftId}) */",
                            )
                            out.println(
                                "    node_${lnode.hashCode().toUInt()} -> node_${rnode.hashCode().toUInt()} [label=\"${totalCost}\"];",
                            )
                        }
                    }
                }

                out.println("}")
            }
        }
    }

    data class Node(
        val surface: String,
        val length: Int,
        val wordEntry: WordEntry?,
        // 最小コスト
        var minCost: Int = Int.MAX_VALUE,
        // 最小コスト経路(直近のみ保存)
        var minPrev: Node? = null,
    )
}
