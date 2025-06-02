package me.filby.neptune.runescript.compiler.type

/**
 * An enumeration of the core types supported by the RuneScript language. These types are the low level representation
 * of other types. All [PrimitiveType]s are one of these.
 */
public enum class BaseVarType {
    INTEGER,
    STRING,
    LONG,
    ARRAY,
    ;

    /**
     * Returns which stack the base type uses.
     */
    public val stackType: StackType
        get() = when (this) {
            INTEGER -> StackType.INTEGER
            STRING -> StackType.OBJECT
            LONG -> StackType.LONG
            ARRAY -> StackType.OBJECT
        }
}
