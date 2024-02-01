package me.filby.neptune.runescript.compiler.trigger

import me.filby.neptune.runescript.compiler.pointer.PointerType
import me.filby.neptune.runescript.compiler.type.Type

/**
 * A trigger type of a script. The trigger type is the first part of a script declaration (`[trigger,_]`) where
 * each trigger has different functionality and uses.
 */
public interface TriggerType {
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
     * The [SubjectMode] for the trigger.
     */
    public val subjectMode: SubjectMode

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

    /**
     * The pointers that the trigger has by default.
     */
    public val pointers: Set<PointerType>?
}
