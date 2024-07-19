package io.github.tokuhirom.kdary.samples.momiji.engine

data class Connection(
    val leftContextId: Int, // 前件文脈ID
    val rightContextId: Int, // 後件文脈ID
    val cost: Short, // コスト
) {
    override fun toString(): String = "$leftContextId $rightContextId $cost"

    companion object {
        fun parse(line: String): Connection {
            val columns = line.split(" ")
            check(columns.size == 3) { "Invalid connection format: '$line'" }
            return Connection(
                leftContextId = columns[0].toInt(),
                rightContextId = columns[1].toInt(),
                cost = columns[2].toShort(),
            )
        }
    }
}
