package me.filby.neptune.runescript.compiler.symbol

import me.filby.neptune.runescript.compiler.trigger.TriggerType
import me.filby.neptune.runescript.compiler.type.Type

public sealed class SymbolType<T : Symbol> {
    // script specific
    public data class ServerScript(public val type: TriggerType) : SymbolType<ScriptSymbol.ServerScriptSymbol>()
    public data class ClientScript(public val type: TriggerType) : SymbolType<ScriptSymbol.ClientScriptSymbol>()
    public data object LocalVariable : SymbolType<LocalVariableSymbol>()

    // global
    public data class Basic(public val type: Type) : SymbolType<BasicSymbol>()
    public data object Constant : SymbolType<ConstantSymbol>()

    override fun toString(): String = this::class.java.simpleName
}
