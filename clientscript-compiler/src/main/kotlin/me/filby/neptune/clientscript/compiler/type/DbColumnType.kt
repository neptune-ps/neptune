package me.filby.neptune.clientscript.compiler.type

import me.filby.neptune.runescript.compiler.type.BaseVarType
import me.filby.neptune.runescript.compiler.type.MutableTypeOptions
import me.filby.neptune.runescript.compiler.type.Type
import me.filby.neptune.runescript.compiler.type.TypeOptions
import me.filby.neptune.runescript.compiler.type.wrapped.WrappedType

/**
 * Represents a database column. The [inner] type is what is returned when accessing
 * the data of a row.
 */
class DbColumnType(override val inner: Type) : WrappedType {
    override val representation: String = "dbcolumn<${inner.representation}>"
    override val code: Char? = null
    override val baseType: BaseVarType = BaseVarType.INTEGER
    override val defaultValue: Any = -1
    override val options: TypeOptions = MutableTypeOptions(
        allowSwitch = false,
        allowArray = false,
        allowDeclaration = false,
    )
}
