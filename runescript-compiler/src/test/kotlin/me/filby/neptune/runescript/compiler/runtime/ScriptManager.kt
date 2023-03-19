package me.filby.neptune.runescript.compiler.runtime

import me.filby.neptune.runescript.compiler.symbol.ScriptSymbol
import me.filby.neptune.runescript.compiler.symbol.Symbol
import me.filby.neptune.runescript.compiler.writer.BaseScriptWriter
import me.filby.neptune.runescript.runtime.Script
import me.filby.neptune.runescript.runtime.impl.ScriptProvider

class ScriptManager : ScriptProvider, BaseScriptWriter.IdProvider {
    private val scripts = mutableMapOf<Int, Script>()

    private val names = mutableListOf<String>()

    private fun findOrGenerateId(name: String): Int {
        val id = names.indexOf(name)
        if (id != -1) {
            return id
        }
        names += name
        println("Created $name with id of ${names.size - 1}")
        return names.size - 1
    }

    fun add(script: Script) {
        val id = findOrGenerateId(script.name ?: error("script name is null"))
        scripts[id] = script
    }

    override fun get(id: Int): Script {
        val script = scripts[id]
        if (script != null) {
            return script
        }
        error("Script $id not found.")
    }

    override fun get(symbol: Symbol): Int {
        if (symbol is ScriptSymbol) {
            return findOrGenerateId("[${symbol.trigger.identifier},${symbol.name}]")
        }
        error("Unsupported symbol: $symbol")
    }

    fun getOrNull(name: String): Script? {
        val id = names.indexOf(name)
        if (id == -1) {
            return null
        }
        return scripts[id]
    }
}
