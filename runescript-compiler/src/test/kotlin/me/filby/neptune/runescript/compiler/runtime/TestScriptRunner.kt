package me.filby.neptune.runescript.compiler.runtime

import me.filby.neptune.runescript.runtime.Script
import me.filby.neptune.runescript.runtime.impl.BaseScriptRunner
import me.filby.neptune.runescript.runtime.impl.ScriptProvider
import me.filby.neptune.runescript.runtime.state.ScriptState

class TestScriptRunner() : BaseScriptRunner<ScriptState>(), ScriptProvider {
    override fun createState(): ScriptState {
        return ScriptState()
    }

    override fun get(id: Int): Script {
        TODO("Not yet implemented")
    }
}
