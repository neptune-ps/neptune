package me.filby.neptune.runescript.compiler.symbol

import me.filby.neptune.runescript.compiler.trigger.ClientTriggerType
import me.filby.neptune.runescript.compiler.type.PrimitiveType
import me.filby.neptune.runescript.compiler.type.Type

public sealed class SymbolType<T : Symbol> {
    // script specific
    public object ServerScript : SymbolType<ScriptSymbol.ServerScriptSymbol>()
    public data class ClientScript(public val type: ClientTriggerType) : SymbolType<ScriptSymbol.ClientScriptSymbol>()
    public object LocalVariable : SymbolType<LocalVariableSymbol>()

    // global
    public data class Basic(public val type: PrimitiveType) : SymbolType<BasicSymbol>()
    public object Constant : SymbolType<ConstantSymbol>()
    public data class Config(public val type: Type) : SymbolType<ConfigSymbol>()
    public object Component : SymbolType<ComponentSymbol>()

    override fun toString(): String = this::class.java.simpleName
}
