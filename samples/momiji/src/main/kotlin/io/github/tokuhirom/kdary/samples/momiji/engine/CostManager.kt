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
            when (left) {
                is Node.BOS -> 0
                is Node.EOS -> error("Should not reach here")
                is Node.Word -> left.wordEntry?.rightId ?: return 0
            }
        val rightLeftId =
            when (right) {
                is Node.BOS -> error("Should not reach here")
                is Node.EOS -> 0
                is Node.Word -> right.wordEntry?.leftId ?: return 0
            }

        return connections.getOrDefault(leftRightId to rightLeftId, 0)
    }
}
