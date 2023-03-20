package me.filby.neptune.runescript.compiler.type

/**
 * Defines options to enable or disable features for a specific [Type].
 */
public sealed interface TypeOptions {
    /**
     * Whether the type is allowed to be used within a switch statement.
     *
     * Default: `true`
     */
    public val allowSwitch: Boolean

    /**
     * Whether the type is allowed to be used in an array.
     *
     * Default: `true`
     */
    public val allowArray: Boolean

    /**
     * Whether the type is allowed to be declared as a parameter or local variable.
     *
     * Default: `true`
     */
    public val allowDeclaration: Boolean
}

/**
 * Implementation of [TypeOptions] with the properties mutable.
 */
public class MutableTypeOptions : TypeOptions {
    override var allowSwitch: Boolean = true
    override var allowArray: Boolean = true
    override var allowDeclaration: Boolean = true
}
