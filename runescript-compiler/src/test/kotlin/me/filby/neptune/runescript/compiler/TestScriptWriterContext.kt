package me.filby.neptune.runescript.compiler

import me.filby.neptune.runescript.compiler.codegen.script.RuneScript
import me.filby.neptune.runescript.compiler.type.StackType
import me.filby.neptune.runescript.compiler.writer.BaseScriptWriter.BaseScriptWriterContext
import me.filby.neptune.runescript.compiler.writer.BaseScriptWriter.Companion.getLocalCount
import me.filby.neptune.runescript.compiler.writer.BaseScriptWriter.Companion.getParameterCount
import me.filby.neptune.runescript.runtime.Script

class TestScriptWriterContext(script: RuneScript, private val arraysV2: Boolean) : BaseScriptWriterContext(script) {
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
        val intParameterCount = script.locals.getParameterCount(StackType.INTEGER, arraysV2)
        val objParameterCount = script.locals.getParameterCount(StackType.OBJECT, arraysV2)
        val longParameterCount = script.locals.getParameterCount(StackType.LONG, arraysV2)
        val intLocalCount = script.locals.getLocalCount(StackType.INTEGER, arraysV2)
        val objLocalCount = script.locals.getLocalCount(StackType.OBJECT, arraysV2)
        val longLocalCount = script.locals.getLocalCount(StackType.LONG, arraysV2)

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
