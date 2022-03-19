package me.filby.neptune.runescript.symbol

import me.filby.neptune.runescript.type.PrimitiveType

public sealed class SymbolType<T : Symbol> {
    public object ServerScript : SymbolType<ServerScriptSymbol>()
    public object ClientScript : SymbolType<ClientScriptSymbol>()
    public data class Config(public val type: PrimitiveType) : SymbolType<ConfigSymbol>()

    override fun toString(): String = this::class.java.simpleName
}
