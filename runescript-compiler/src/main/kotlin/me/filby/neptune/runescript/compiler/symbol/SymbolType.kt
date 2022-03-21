package me.filby.neptune.runescript.compiler.symbol

import me.filby.neptune.runescript.compiler.trigger.ClientTriggerType
import me.filby.neptune.runescript.compiler.type.PrimitiveType

public sealed class SymbolType<T : Symbol> {
    // script specific
    public object ServerScript : SymbolType<ServerScriptSymbol>()
    public data class ClientScript(public val type: ClientTriggerType) : SymbolType<ClientScriptSymbol>()
    public object LocalVariable : SymbolType<LocalVariableSymbol>()

    // global
    public data class Config(public val type: PrimitiveType) : SymbolType<ConfigSymbol>()
    public object Component : SymbolType<ComponentSymbol>()

    override fun toString(): String = this::class.java.simpleName
}
