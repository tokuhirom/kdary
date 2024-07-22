package io.github.tokuhirom.kdary.samples.momiji.engine

import io.github.tokuhirom.kdary.samples.momiji.entity.WordEntry

sealed class Node(
    open val surface: String,
    open val length: Int,
    open val wordEntry: WordEntry?,
    // 最小コスト
    open var minCost: Int = Int.MAX_VALUE,
    // 最小コスト経路(直近のみ保存)
    open var minPrev: Node? = null,
) {
    class BOS : Node("__BOS__", 0, null)

    class EOS : Node("__EOS__", 0, null)

    data class Word(
        override val surface: String,
        override val length: Int,
        override val wordEntry: WordEntry?,
        override var minCost: Int = Int.MAX_VALUE,
        override var minPrev: Node? = null,
    ) : Node(surface, length, wordEntry, minCost, minPrev)
}
