package me.filby.neptune.runescript.compiler.type

import com.google.common.base.MoreObjects
import me.filby.neptune.runescript.type.BaseVarType
import me.filby.neptune.runescript.type.Type

/**
 * A [Type] that represents an array of another type.
 */
public data class ArrayType(public val type: Type) : Type {
    init {
        assert(type !is ArrayType)
    }

    override val representation: String = "${type.representation}array"

    override val code: Char
        get() = error("ArrayType has no character representation.")

    override val baseType: BaseVarType
        get() = error("ArrayType has no BaseVarType.")

    override val defaultValue: Any
        get() = error("ArrayType has no default value.")

    override fun toString(): String {
        return MoreObjects.toStringHelper(this)
            .add("type", type)
            .toString()
    }
}
