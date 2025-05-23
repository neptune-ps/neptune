package me.filby.neptune.runescript.compiler.symbol

import me.filby.neptune.runescript.compiler.type.Type

/**
 * A basic representation of a symbol for RuneScript. A symbol can represent
 * anything that is able to be referenced within a script or config file.
 *
 * @see SymbolTable
 */
public sealed interface Symbol {
    /**
     * The name of the symbol, which is used for lookup in a [SymbolTable].
     */
    public val name: String
}

// script local variables
public data class LocalVariableSymbol(override val name: String, val type: Type) : Symbol

// symbols with constant values, new ones should also be included in TypeChecking.isConstantSymbol
public data class BasicSymbol(override val name: String, val type: Type) : Symbol
public data class ConstantSymbol(override val name: String, val value: String) : Symbol
