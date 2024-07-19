package io.github.tokuhirom.kdary.samples.momiji.entity

data class WordEntry(
    val surface: String, // 表層形
    val leftId: Int, // 左文脈ID
    val rightId: Int, // 右文脈ID
    val cost: Int, // 単語コスト
    val annotations: List<String>, // その他のカラム
) {
    // return as a csv format
    override fun toString(): String =
        listOf(
            surface,
            leftId,
            rightId,
            cost,
            annotations.joinToString(","),
        ).joinToString(",")

    companion object {
        fun parse(line: String): WordEntry {
            val columns = line.split(",")
            return WordEntry(
                surface = columns[0],
                leftId = columns[1].toInt(),
                rightId = columns[2].toInt(),
                cost = columns[3].toInt(),
                annotations = columns.drop(4),
            )
        }
    }
}
