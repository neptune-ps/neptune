package me.filby.neptune.runescript

// TODO move these to a shared module and allow registering types at runtime (will require lexer changes)

/**
 * An enumeration of the base types that are supported by RuneScripts, ClientScripts, and configs.
 */
public enum class BaseVarType {
    INTEGER,
    STRING,
    LONG
}

/**
 * Represents a type that is available to RuneScript, ClientScript, and configs.
 */
public enum class ScriptVarType(
    public val charKey: Char,
    public val fullName: String,
    public val defaultValue: Any,
    public val baseVarType: BaseVarType = BaseVarType.INTEGER,
) {
    // only supporting the basic types for now
    INTEGER('i', "int", 0),
    STRING('s', "string", "", BaseVarType.STRING),
    LONG('Ï', "long", 0L, BaseVarType.LONG);

    public companion object {

        /**
         * A map of names to the type.
         */
        private val nameToType = values().associateBy { it.fullName }

        /**
         * Looks up a [ScriptVarType] by name.
         *
         * @return The [ScriptVarType] if it exists, otherwise `null`.
         */
        public fun lookup(name: String): ScriptVarType? {
            return nameToType[name]
        }

    }

}
