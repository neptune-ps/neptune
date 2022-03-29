package me.filby.neptune.runescript.compiler.symbol

import me.filby.neptune.runescript.compiler.trigger.TriggerType
import me.filby.neptune.runescript.compiler.type.Type

/**
 * A script symbol is a type of symbol that defines any type of script. Each script
 * must define its trigger type, name, parameter type(s), and return type(s).
 */
public sealed interface ScriptSymbol : Symbol {
    /**
     * The trigger type of the script.
     */
    public val trigger: TriggerType

    /**
     * The name of the script.
     */
    override val name: String

    /**
     * The type needed for the scripts parameters. If `null`, the script has no
     * defined parameters. If the script has multiple parameters, `TupleType`
     * may be used.
     *
     * @see me.filby.neptune.runescript.compiler.type.TupleType
     */
    public val parameters: Type?

    /**
     * The type that the script returns. If `null`, the script has no return
     * types. If the script has multiple returns, `TupleType` may be used.
     *
     * @see me.filby.neptune.runescript.compiler.type.TupleType
     */
    public val returns: Type?

    /**
     * A [ScriptSymbol] type specific for server sided scripts.
     */
    public data class ServerScriptSymbol(
        override val trigger: TriggerType,
        override val name: String,
        override val parameters: Type?,
        override val returns: Type
    ) : ScriptSymbol

    /**
     * A [ScriptSymbol] type specific for client sided scripts.
     */
    public data class ClientScriptSymbol(
        override val trigger: TriggerType,
        override val name: String,
        override val parameters: Type?,
        override val returns: Type
    ) : ScriptSymbol
}
