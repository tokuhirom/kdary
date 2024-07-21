package io.github.tokuhirom.kdary.samples.momiji.engine

import io.github.tokuhirom.kdary.samples.momiji.entity.WordEntry
import java.io.File

/**
 * Every index should be the character index.
 */
class Lattice(
    private val sentence: String,
    private val costManager: CostManager,
) {
    private val beginNodes: MutableList<MutableList<Node>> = mutableListOf()
    private val endNodes: MutableList<MutableList<Node>> = mutableListOf()

    init {
        // +1 for EOS node
        for (i in 0 until sentence.length + 1) {
            beginNodes.add(mutableListOf())
            endNodes.add(mutableListOf())
        }

        // register BOS node
        endNodes[0].add(Node.BOS())

        // register EOS node
        beginNodes[sentence.length].add(Node.EOS())
    }

    fun insert(
        begin: Int,
        end: Int,
        wordEntry: WordEntry? = null,
    ): Node {
        val node =
            Node.Word(
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
