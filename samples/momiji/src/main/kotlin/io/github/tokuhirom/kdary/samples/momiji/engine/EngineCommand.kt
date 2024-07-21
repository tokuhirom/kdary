package io.github.tokuhirom.kdary.samples.momiji.engine

import com.github.ajalt.clikt.core.CliktCommand

class EngineCommand : CliktCommand() {
    private val dictDir = "dict"

    override fun run() {
        val engine = MomijiEngineLoader(dictDir).load()
//        engine.analysis("東京都")
//        engine.analysis("自然言語")
        engine.analysis("吾輩はネコである。").forEach {
            println(it)
        }
    }
}
