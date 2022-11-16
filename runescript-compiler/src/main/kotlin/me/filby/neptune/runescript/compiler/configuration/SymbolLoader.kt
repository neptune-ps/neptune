package me.filby.neptune.runescript.compiler.configuration

import me.filby.neptune.runescript.compiler.ScriptCompiler
import me.filby.neptune.runescript.compiler.symbol.BasicSymbol
import me.filby.neptune.runescript.compiler.symbol.ConfigSymbol
import me.filby.neptune.runescript.compiler.symbol.ConstantSymbol
import me.filby.neptune.runescript.compiler.symbol.SymbolTable
import me.filby.neptune.runescript.compiler.symbol.SymbolType
import me.filby.neptune.runescript.compiler.type.PrimitiveType
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
    public fun load(compiler: ScriptCompiler, rootTable: SymbolTable)

    // helper extension functions for adding things to symbol table

    /**
     * Adds a [ConstantSymbol] to the table with the given [name] and [value].
     */
    public fun SymbolTable.addConstant(name: String, value: String): Boolean {
        return insert(SymbolType.Constant, ConstantSymbol(name, value))
    }

    /**
     * Adds a [ConfigSymbol] to the table with the given [type] and [name].
     */
    public fun SymbolTable.addConfig(type: Type, name: String): Boolean {
        return insert(SymbolType.Config(type), ConfigSymbol(name, type))
    }

    /**
     * Adds a [BasicSymbol] to the table with a type of [PrimitiveType.COMPONENT] and [name].
     */
    public fun SymbolTable.addComponent(name: String): Boolean {
        return addBasic(PrimitiveType.COMPONENT, name)
    }

    /**
     * Adds a [BasicSymbol] to the table with the given [type] and [name]. This
     * should be used for any non-config symbols that don't have any special properties
     * to them.
     */
    public fun SymbolTable.addBasic(type: Type, name: String): Boolean {
        return insert(SymbolType.Basic(type), BasicSymbol(name, type))
    }
}
