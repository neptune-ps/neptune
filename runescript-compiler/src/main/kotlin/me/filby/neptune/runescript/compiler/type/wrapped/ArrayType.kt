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

    override val code: Char = when (inner.baseType) {
        BaseVarType.INTEGER -> INTARRAY_CHAR
        BaseVarType.STRING -> STRINGARRAY_CHAR
        else -> error("Invalid type: $inner")
    }

    override val baseType: BaseVarType = BaseVarType.ARRAY

    override val defaultValue: Any? = null

    override fun toString(): String = MoreObjects.toStringHelper(this)
        .add("inner", inner)
        .toString()

    private companion object {
        // these two values actually refer to intarray and componentarray characters,
        // but with the array rework their char codes were repurposed for type encoding
        // to signify "int-based" and "string-based" arrays.
        const val INTARRAY_CHAR = 'W'
        const val STRINGARRAY_CHAR = 'X'
    }
}
