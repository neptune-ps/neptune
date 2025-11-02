package me.filby.neptune.clientscript.compiler.type

import com.google.common.base.MoreObjects
import me.filby.neptune.runescript.compiler.type.BaseVarType
import me.filby.neptune.runescript.compiler.type.MetaType
import me.filby.neptune.runescript.compiler.type.MutableTypeOptions
import me.filby.neptune.runescript.compiler.type.Type
import me.filby.neptune.runescript.compiler.type.TypeOptions
import me.filby.neptune.runescript.compiler.type.wrapped.WrappedType

class IfScriptType(override val inner: Type) : WrappedType {
    override val representation: String
        get() = when (inner) {
            MetaType.Any -> "ifscript"
            else -> "ifscript<${inner.representation}>"
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
