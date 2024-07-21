package io.github.tokuhirom.kdary.samples.momiji.engine

import io.github.tokuhirom.kdary.samples.momiji.entity.WordEntry

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
