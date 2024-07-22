package io.github.tokuhirom.kdary.samples.momiji.engine

import com.github.ajalt.clikt.core.CliktCommand

class EngineCommand : CliktCommand() {
    private val dictDir = "dict"

    override fun run() {
        val engine = MomijiEngineLoader(dictDir).load()
//        engine.analysis("東京都")
//        engine.analysis("自然言語")
//        val sample = "吾輩はネコである。"
//        val sample = "Taiyaki"
        val sample = "Taiyakiは形態素解析エンジンである"

        val lattice = engine.buildLattice(sample)
        lattice.viterbi().forEachIndexed { index, node ->
            val transitionCost =
                node.minPrev?.let { prev ->
                    engine.costManager.getTransitionCost(prev, node)
                } ?: 0
            println(
                String.format(
                    "%3d transition=%-10d emission=%-10d %-20s %s",
                    index,
                    transitionCost,
                    node.wordEntry?.cost,
                    node.surface,
                    node.wordEntry?.annotations?.joinToString(","),
                ),
            )
        }
    }
}
