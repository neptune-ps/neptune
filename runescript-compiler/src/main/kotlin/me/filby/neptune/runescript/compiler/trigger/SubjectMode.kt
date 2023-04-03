package me.filby.neptune.runescript.compiler.trigger

import me.filby.neptune.runescript.compiler.type.Type as ScriptType

/**
 * Determines how a script subject is validated.
 */
public sealed interface SubjectMode {
    /**
     * A subject mode that only allows global (`_`) scripts.
     */
    public object None : SubjectMode

    /**
     * A subject mode specifies the subject as just part of the script name and is
     * not a reference to a symbol.
     */
    public object Name : SubjectMode

    /**
     * A subject mode that specifies the subject is a `Type` of some sort.
     */
    public data class Type(
        public val type: ScriptType,
        public val category: Boolean = true,
        public val global: Boolean = true,
    ) : SubjectMode
}
