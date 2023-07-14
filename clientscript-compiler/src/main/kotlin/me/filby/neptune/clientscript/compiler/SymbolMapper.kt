package me.filby.neptune.clientscript.compiler

import me.filby.neptune.runescript.compiler.symbol.ScriptSymbol
import me.filby.neptune.runescript.compiler.symbol.Symbol
import me.filby.neptune.runescript.compiler.trigger.CommandTrigger
import me.filby.neptune.runescript.compiler.writer.BaseScriptWriter

/**
 * A [BaseScriptWriter.IdProvider] that allows looking up an id by [Symbol].
 *
 * This implementation has a map for [ScriptSymbol]s and one for all other symbols.
 * The script symbol stores the key as the full name of the script instead of the
 * symbol itself.
 */
class SymbolMapper : BaseScriptWriter.IdProvider {
    /**
     * A special mapping for scripts since command symbols are created in the compiler.
     */
    private val commands = mutableMapOf<String, Int>()

    /**
     * A special mapping for scripts since script symbols are created in the compiler.
     */
    private val scripts = mutableMapOf<String, Int>()

    /**
     * A map of each symbol to their id.
     */
    private val symbols = mutableMapOf<Symbol, Int>()

    fun putSymbol(id: Int, symbol: Symbol) {
        val existing = symbols.putIfAbsent(symbol, id)
        if (existing != null) {
            error("Duplicate symbol: $symbol")
        }
    }

    fun putCommand(id: Int, name: String) {
        val existing = commands.putIfAbsent(name, id)
        if (existing != null) {
            error("Duplicate command: $name")
        }
    }

    fun putScript(id: Int, name: String) {
        val existing = scripts.putIfAbsent(name, id)
        if (existing != null) {
            error("Duplicate script: $name")
        }
    }

    override fun get(symbol: Symbol): Int {
        if (symbol is ScriptSymbol) {
            return if (symbol.trigger == CommandTrigger) {
                // trim off dot commands
                val name = symbol.name.substringAfter('.')
                commands[name] ?: error("Unable to find id for $symbol")
            } else {
                val name = "[${symbol.trigger.identifier},${symbol.name}]"
                scripts[name] ?: error("Unable to find id for $symbol")
            }
        }
        return symbols[symbol] ?: error("Unable to find id for $symbol")
    }
}
