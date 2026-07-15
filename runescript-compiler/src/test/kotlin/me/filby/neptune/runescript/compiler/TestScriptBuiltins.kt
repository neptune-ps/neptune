package me.filby.neptune.runescript.compiler

import me.filby.neptune.runescript.compiler.type.Type

object TestScriptBuiltins : CompilerBuiltins {
    override val int: Type = ScriptVarType.INT
    override val boolean: Type = ScriptVarType.BOOLEAN
    override val string: Type = ScriptVarType.STRING
    override val char: Type = ScriptVarType.CHAR
    override val long: Type = ScriptVarType.LONG
    override val coordgrid: Type = ScriptVarType.COORD
}
