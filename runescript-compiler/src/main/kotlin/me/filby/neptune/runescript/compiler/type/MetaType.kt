package me.filby.neptune.runescript.compiler.type

/**
 * An enumeration of types used internally in the compiler and should not
 * be exposed at all.
 */
public enum class MetaType : Type {
    /**
     * A type used to specify the type resolution resulted into an error. This
     * type is comparable to **all** other types to prevent error propagation.
     */
    ERROR,

    /**
     * A type that represents a `null` literal.
     */
    NULL,
    ;

    override val representation: String
        get() = name.lowercase()

    override val code: Char
        get() = error("MetaType has no character representation.")

    // all meta types are represented as an integer
    override val baseType: BaseVarType = BaseVarType.INTEGER

    // all meta types have a default value of `null` (-1).
    override val defaultValue: Any = -1
}
