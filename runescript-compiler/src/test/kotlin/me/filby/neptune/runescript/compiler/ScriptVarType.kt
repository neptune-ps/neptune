package me.filby.neptune.runescript.compiler

import me.filby.neptune.runescript.compiler.type.BaseVarType
import me.filby.neptune.runescript.compiler.type.MutableTypeOptions
import me.filby.neptune.runescript.compiler.type.Type
import me.filby.neptune.runescript.compiler.type.TypeBuilder
import me.filby.neptune.runescript.compiler.type.TypeOptions

enum class ScriptVarType(
    override val code: Char,
    override val baseType: BaseVarType = BaseVarType.INTEGER,
    override val defaultValue: Any,
    builder: TypeBuilder? = null,
) : Type {
    INT('i', defaultValue = 0),
    BOOLEAN('1', defaultValue = 0),
    COORD('c', defaultValue = -1),
    STRING('s', BaseVarType.STRING, defaultValue = "", {
        allowArray = true
        allowSwitch = false
    }),
    CHAR('z', defaultValue = -1),
    LONG('Ï', BaseVarType.LONG, defaultValue = 0L, {
        allowArray = false
        allowSwitch = false
    }),
    ;

    override val representation: String = name.lowercase()
    override val options: TypeOptions = MutableTypeOptions().apply { builder?.invoke(this) }
}
