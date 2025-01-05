package me.filby.neptune.runescript.compiler.runtime

import me.filby.neptune.runescript.runtime.impl.BaseScriptRunner
import me.filby.neptune.runescript.runtime.state.ScriptState

class TestScriptRunner : BaseScriptRunner<ScriptState>() {
    override fun createState(): ScriptState = ScriptState()
}
