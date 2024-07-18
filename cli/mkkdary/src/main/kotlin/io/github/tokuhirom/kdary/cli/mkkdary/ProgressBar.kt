package io.github.tokuhirom.kdary.cli.mkkdary

class ProgressBar(
    private val total: Int,
) {
    private var prevPercentage: Int = 0

    fun update(current: Int): Int {
        val curPercentage = (100.0 * current / total).toInt()
        val barLen = (1.0 * current * SCALE / total).toInt()

        if (prevPercentage != curPercentage) {
            print(
                "Making double-array: %3d%% |%s%s|".format(
                    curPercentage,
                    Companion.BAR.substring(0, barLen),
                    " ".repeat(SCALE - barLen),
                ),
            )
            if (curPercentage >= 100) {
                println()
            } else {
                print("\r")
            }
            System.out.flush()
        }

        prevPercentage = curPercentage

        return 1
    }

    companion object {
        private const val BAR = "*******************************************"
        private const val SCALE = BAR.length
    }
}
