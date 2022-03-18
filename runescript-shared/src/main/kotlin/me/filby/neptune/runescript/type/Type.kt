package me.filby.neptune.runescript.type

/**
 * Represents a type that we use in the type system to make sure everything is only assigned the correct thing.
 *
 * @see PrimitiveType
 * @see TupleType
 */
public interface Type {
    /**
     * A string used to represent the type. This is what is used in scripts to reference it. E.g. `def_int` or `int`
     * would rely on there being a type with a representation of `int`.
     */
    public val representation: String

    /**
     * The character representation of the type.
     */
    public val code: Char?

    /**
     * The base type of the type. This type determines which stack the type uses.
     */
    public val baseType: BaseVarType?

    /**
     * The default value of the type.
     */
    public val defaultValue: Any?
}
