package me.filby.neptune.clientscript.compiler.type

import me.filby.neptune.runescript.compiler.type.BaseVarType
import me.filby.neptune.runescript.compiler.type.Type

enum class ScriptVarType(
    override val code: Char?,
    override val baseType: BaseVarType = BaseVarType.INTEGER,
    override val defaultValue: Any?,
) : Type {
    COMPONENT('I', defaultValue = -1),
    NAMEDOBJ('O', defaultValue = -1),
    STAT('S', defaultValue = -1),
    NPC_STAT('T', defaultValue = -1),
    GRAPHIC('d', defaultValue = -1),
    ENUM('g', defaultValue = -1),
    OBJ('o', defaultValue = -1),
    MAPELEMENT('µ', defaultValue = -1),
    INV('v', defaultValue = -1),
    ;

    override val representation: String = name.lowercase()
}
