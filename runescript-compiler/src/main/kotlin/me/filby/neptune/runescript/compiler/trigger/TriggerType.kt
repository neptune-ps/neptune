package me.filby.neptune.runescript.compiler.trigger

import me.filby.neptune.runescript.type.Type

/**
 * A trigger type of a script. The trigger type is the first part of a script declaration (`[trigger,_]`) where
 * each trigger has different functionality and uses.
 */
public sealed interface TriggerType {
    /**
     * A unique number to identify the trigger.
     */
    public val id: Int

    /**
     * The text that represents the trigger. This is the string that is used to identifier the trigger when defining a
     * script.
     *
     * ```
     * [<identifier>,<subject>]
     * ```
     */
    public val identifier: String

    /**
     * The expected type of the subject. If `null` then the subject is just treated as a unique name. If not `null`
     * then the subject must be the defined type, `category` (represented as `_<category>`), or a global
     * (represented as `_`).
     */
    public val subjectType: Type?

    /**
     * Whether parameters are allowed in scripts using the trigger.
     */
    public val allowParameters: Boolean

    /**
     * The parameters that must be defined. If `null` no arguments are expected.
     */
    public val parameters: Type?

    /**
     * Whether returns are allowed in scripts using the trigger.
     */
    public val allowReturns: Boolean

    /**
     * The return types that must be defined. If `null` no returns are expected.
     */
    public val returns: Type?
}
