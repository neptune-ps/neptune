package me.filby.neptune.runescript.compiler.type

/**
 * An enumeration of the core types supported by the RuneScript language. These types are the low level representation
 * of other types. All [PrimitiveType]s are one of these.
 */
public enum class BaseVarType(public val id: Int) {
    INTEGER(0),
    LONG(1),
    STRING(2),
    ARRAY(5),
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
