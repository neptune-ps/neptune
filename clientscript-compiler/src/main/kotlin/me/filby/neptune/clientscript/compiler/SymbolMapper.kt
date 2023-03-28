package me.filby.neptune.clientscript.compiler

import me.filby.neptune.runescript.compiler.symbol.ScriptSymbol
import me.filby.neptune.runescript.compiler.symbol.Symbol
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

    fun putScript(id: Int, name: String) {
        val existing = scripts.putIfAbsent(name, id)
        if (existing != null) {
            error("Duplicate script: $name")
        }
    }

    override fun get(symbol: Symbol): Int {
        if (symbol is ScriptSymbol) {
            val name = "[${symbol.trigger.identifier},${symbol.name}]"
            return scripts[name] ?: error("Unable to find id for $symbol")
        }
        return symbols[symbol] ?: error("Unable to find id for $symbol")
    }
}
