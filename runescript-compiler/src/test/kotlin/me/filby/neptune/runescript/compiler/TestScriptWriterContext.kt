package me.filby.neptune.runescript.compiler

import me.filby.neptune.runescript.compiler.codegen.script.RuneScript
import me.filby.neptune.runescript.compiler.type.BaseVarType
import me.filby.neptune.runescript.compiler.writer.BaseScriptWriter.BaseScriptWriterContext
import me.filby.neptune.runescript.compiler.writer.BaseScriptWriter.Companion.getLocalCount
import me.filby.neptune.runescript.compiler.writer.BaseScriptWriter.Companion.getParameterCount
import me.filby.neptune.runescript.runtime.Script

class TestScriptWriterContext(script: RuneScript) : BaseScriptWriterContext(script) {
    private val opcodes = mutableListOf<Int>()
    private val intOperands = mutableListOf<Int>()
    private val objOperands = mutableListOf<Any?>()
    private val switchTables = mutableListOf<Map<Int, Int>>()

    fun instruction(opcode: Int, operand: Int = 0) {
        opcodes += opcode
        intOperands += operand
        objOperands += null
    }

    fun instruction(opcode: Int, operand: String) {
        opcodes += opcode
        intOperands += 0
        objOperands += operand
    }

    fun instruction(opcode: Int, operand: Long) {
        opcodes += opcode
        intOperands += 0
        objOperands += operand
    }

    fun switchTable(id: Int, cases: Map<Int, Int>) {
        switchTables.add(id, cases)
    }

    fun build(): Script {
        val intParameterCount = script.locals.getParameterCount(BaseVarType.INTEGER)
        val objParameterCount = script.locals.getParameterCount(BaseVarType.STRING)
        val longParameterCount = script.locals.getParameterCount(BaseVarType.LONG)
        val intLocalCount = script.locals.getLocalCount(BaseVarType.INTEGER)
        val objLocalCount = script.locals.getLocalCount(BaseVarType.STRING)
        val longLocalCount = script.locals.getLocalCount(BaseVarType.LONG)

        return Script(
            Script.SourceInfo(script.fullName, script.sourceName, lineNumberTable),
            opcodes.toIntArray(),
            intParameterCount,
            objParameterCount,
            longParameterCount,
            intLocalCount,
            objLocalCount,
            longLocalCount,
            intOperands.toIntArray(),
            objOperands.toTypedArray(),
            switchTables,
        )
    }
}
