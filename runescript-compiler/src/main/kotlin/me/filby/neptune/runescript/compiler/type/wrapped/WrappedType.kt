package me.filby.neptune.runescript.compiler.type.wrapped

import me.filby.neptune.runescript.compiler.type.Type

/**
 * A type that container an inner type. This is intended for more complex types
 * that resolve to a different type depending on how they're accessed. This is
 * necessary for some cases where you want to verify a reference is of a type
 * without seeing that the "execution" type would be.
 *
 * @see ArrayType
 */
public sealed interface WrappedType : Type {
    /**
     * The inner type that is being wrapped.
     */
    public val inner: Type
}
