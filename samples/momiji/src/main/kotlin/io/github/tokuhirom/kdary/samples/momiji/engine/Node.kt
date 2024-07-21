package io.github.tokuhirom.kdary.samples.momiji.engine

import io.github.tokuhirom.kdary.samples.momiji.entity.WordEntry

data class Node(
    val surface: String,
    val length: Int,
    val wordEntry: WordEntry?,
    // 最小コスト
    var minCost: Int = Int.MAX_VALUE,
    // 最小コスト経路(直近のみ保存)
    var minPrev: Node? = null,
)
