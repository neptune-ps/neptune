package me.filby.neptune.runescript.compiler.configuration

import me.filby.neptune.runescript.compiler.ScriptCompiler
import me.filby.neptune.runescript.compiler.symbol.BasicSymbol
import me.filby.neptune.runescript.compiler.symbol.ConfigSymbol
import me.filby.neptune.runescript.compiler.symbol.ConstantSymbol
import me.filby.neptune.runescript.compiler.symbol.SymbolTable
import me.filby.neptune.runescript.compiler.symbol.SymbolType
import me.filby.neptune.runescript.compiler.type.Type

/**
 * A loader that gets ran before any compilation process happens with the compiler. This
 * allows custom implementations of external symbol loading. This interface defines helper
 * extension functions for adding specific types of symbols to a [SymbolTable].
 */
public interface SymbolLoader {
    /**
     * Called when the compiler is ready to load external symbols.
     *
     * Types may be looked up via [ScriptCompiler.types] if needed.
     */
    public fun SymbolTable.load(compiler: ScriptCompiler)

    // helper extension functions for adding things to symbol table

    /**
     * Adds a [ConstantSymbol] to the table with the given [name] and [value].
     *
     * Returns [ConstantSymbol] that was inserted.
     */
    public fun SymbolTable.addConstant(name: String, value: String): ConstantSymbol {
        val symbol = ConstantSymbol(name, value)
        if (!insert(SymbolType.Constant, symbol)) {
            error("Unable to add constant: name=$name, value=$value")
        }
        return symbol
    }

    /**
     * Adds a [ConfigSymbol] to the table with the given [type] and [name].
     *
     * Returns the [ConfigSymbol] that was inserted.
     */
    public fun SymbolTable.addConfig(type: Type, name: String): ConfigSymbol {
        val symbol = ConfigSymbol(name, type)
        if (!insert(SymbolType.Config(type), symbol)) {
            error("Unable to add config: type=$type, name=$name")
        }
        return symbol
    }

    /**
     * Adds a [BasicSymbol] to the table with the given [type] and [name]. This
     * should be used for any non-config symbols that don't have any special properties
     * to them.
     *
     * Returns the [BasicSymbol] that was inserted.
     */
    public fun SymbolTable.addBasic(type: Type, name: String): BasicSymbol {
        val symbol = BasicSymbol(name, type)
        if (!insert(SymbolType.Basic(type), symbol)) {
            error("Unable to add basic: type=$type, name=$name")
        }
        return symbol
    }
}
