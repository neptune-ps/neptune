package me.filby.neptune.runescript.compiler.type.wrapped

import com.google.common.base.MoreObjects
import me.filby.neptune.runescript.compiler.type.BaseVarType
import me.filby.neptune.runescript.compiler.type.Type

/**
 * A [Type] that represents an array of another type.
 */
public data class ArrayType(public override val inner: Type) : WrappedType {
    init {
        assert(inner !is ArrayType)
    }

    override val representation: String = "${inner.representation}array"

    override val code: Char
        get() = error("ArrayType has no character representation.")

    override val baseType: BaseVarType
        get() = error("ArrayType has no BaseVarType.")

    override val defaultValue: Any
        get() = error("ArrayType has no default value.")

    override fun toString(): String {
        return MoreObjects.toStringHelper(this)
            .add("inner", inner)
            .toString()
    }
}
