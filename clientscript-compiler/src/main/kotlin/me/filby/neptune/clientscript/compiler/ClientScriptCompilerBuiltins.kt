package me.filby.neptune.clientscript.compiler

import me.filby.neptune.clientscript.compiler.type.ScriptVarType
import me.filby.neptune.runescript.compiler.CompilerBuiltins
import me.filby.neptune.runescript.compiler.type.Type

object ClientScriptCompilerBuiltins : CompilerBuiltins {
    override val int: Type = ScriptVarType.INT
    override val boolean: Type = ScriptVarType.BOOLEAN
    override val string: Type = ScriptVarType.STRING
    override val char: Type = ScriptVarType.CHAR
    override val long: Type = ScriptVarType.LONG
    override val coordgrid: Type = ScriptVarType.COORDGRID
}
