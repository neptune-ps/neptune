package me.filby.neptune.runescript.compiler.runtime

import me.filby.neptune.runescript.runtime.Script
import me.filby.neptune.runescript.runtime.impl.ScriptProvider

class ScriptManager : ScriptProvider {
    private val scripts = mutableMapOf<Int, Script>()

    private val names = mutableListOf<String>()

    fun findOrGenerateId(name: String): Int {
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

    fun getOrNull(name: String): Script? {
        val id = names.indexOf(name)
        if (id == -1) {
            return null
        }
        return scripts[id]
    }
}
