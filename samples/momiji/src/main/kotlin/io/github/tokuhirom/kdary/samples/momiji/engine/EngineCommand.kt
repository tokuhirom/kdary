package io.github.tokuhirom.kdary.samples.momiji.engine

import com.github.ajalt.clikt.core.CliktCommand

class EngineCommand : CliktCommand() {
    private val dictDir = "dict"

    override fun run() {
        val engine = MomijiEngineLoader(dictDir).load()
        println(engine.analysis("吾輩はネコである。"))
    }
}
