package me.filby.neptune.runescript.compiler.type

/**
 * A sealed class of types used internally in the compiler.
 */
public sealed class MetaType(private val name: String) : Type {
    /**
     * A type used to specify the type resolution resulted into an error. This
     * type is comparable to **all** other types to prevent error propagation.
     */
    public object Error : MetaType("error")

    /**
     * A type that signifies that nothing is returned.
     */
    public object Unit : MetaType("unit")

    override val representation: String
        get() = name.lowercase()

    override val code: Char
        get() = error("MetaType has no character representation.")

    // all meta types are represented as an integer
    override val baseType: BaseVarType = BaseVarType.INTEGER

    // all meta types have a default value of `null` (-1).
    override val defaultValue: Any = -1
}
