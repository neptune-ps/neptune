package me.filby.neptune.clientscript.compiler.type

import com.google.common.base.MoreObjects
import me.filby.neptune.runescript.compiler.type.BaseVarType
import me.filby.neptune.runescript.compiler.type.MetaType
import me.filby.neptune.runescript.compiler.type.MutableTypeOptions
import me.filby.neptune.runescript.compiler.type.Type
import me.filby.neptune.runescript.compiler.type.TypeOptions
import me.filby.neptune.runescript.compiler.type.wrapped.WrappedType

class ParamType(override val inner: Type) : WrappedType {
    override val representation = when (inner) {
        MetaType.Any -> "param"
        else -> "param<${inner.representation}>"
    }
    override val code = null
    override val baseType = BaseVarType.INTEGER
    override val defaultValue = -1
    override val options: TypeOptions = MutableTypeOptions(
        allowSwitch = false,
        allowArray = false,
        allowDeclaration = false,
    )

    override fun toString(): String = MoreObjects.toStringHelper(this)
        .add("inner", inner)
        .toString()
}
