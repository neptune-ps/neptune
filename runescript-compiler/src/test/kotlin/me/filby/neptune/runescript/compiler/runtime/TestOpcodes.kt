package me.filby.neptune.runescript.compiler.runtime

import me.filby.neptune.runescript.runtime.impl.Instruction
import me.filby.neptune.runescript.runtime.impl.opcodes.BaseCoreOpcodes
import me.filby.neptune.runescript.runtime.state.ScriptState

@Suppress("unused", "TestFunctionName")
class TestOpcodes {
    @Suppress("UnusedReceiverParameter")
    @Instruction(BaseCoreOpcodes.PRINTLN)
    fun ScriptState._println(string: String) {
        println(string)
    }

    @Instruction(ERROR)
    fun ScriptState._error(mes: String) {
        scriptError("Error: $mes")
    }

    @Instruction(ASSERT_EQUALS)
    fun ScriptState._assert_equals(expected: Int, actual: Int) {
        if (expected != actual) {
            scriptError("assert failure: $expected != $actual")
        }
    }

    @Instruction(ASSERT_EQUALS_OBJ)
    fun ScriptState._assert_equals_obj(expected: Any, actual: Any) {
        if (expected != actual) {
            scriptError("assert failure: $expected != $actual")
        }
    }

    @Instruction(ASSERT_EQUALS_LONG)
    fun ScriptState._assert_equals_long(expected: Long, actual: Long) {
        if (expected != actual) {
            scriptError("assert failure: $expected != $actual")
        }
    }

    @Instruction(ASSERT_NOT)
    fun ScriptState._assert_not(invalid: Int, actual: Int) {
        if (invalid == actual) {
            scriptError("assert failure: $invalid == $actual")
        }
    }

    @Instruction(ASSERT_NOT_OBJ)
    fun ScriptState._assert_not_obj(invalid: Any, actual: Any) {
        if (invalid == actual) {
            scriptError("assert failure: $invalid == $actual")
        }
    }

    @Instruction(ASSERT_NOT_LONG)
    fun ScriptState._assert_not_long(invalid: Long, actual: Long) {
        if (invalid == actual) {
            scriptError("assert failure: $invalid == $actual")
        }
    }

    @Instruction(INT_TO_LONG)
    fun ScriptState._int_to_long(input: Int) {
        pushLong(input.toLong())
    }

    @Instruction(LONG_TO_INT)
    fun ScriptState._long_to_int(input: Long) {
        pushInt(input.toInt())
    }

    @Instruction(COMPARE)
    fun ScriptState._compare(str1: String, str2: String) {
        pushInt(str1.compareTo(str2))
    }

    private fun ScriptState.scriptError(mes: String) {
        val error = buildString {
            append(scriptPath)
            append(":")
            append(findCurrentLineNumber())
            append(": ")
            append(mes)
        }

        println(error)
        execution = ScriptState.ExecutionState.ABORTED
    }

    private val ScriptState.scriptPath: String
        get() {
            val sourceInfo = script.sourceInfo ?: return "<unknown path>"
            return sourceInfo.path ?: "<unknown path>"
        }

    private fun ScriptState.findCurrentLineNumber(): Int {
        val sourceInfo = script.sourceInfo ?: return 0
        val lineNumerTable = sourceInfo.lineNumberTable ?: return 0
        return lineNumerTable.floorEntry(pc).value
    }

    companion object {
        const val ERROR = 5000
        const val ASSERT_EQUALS = ERROR + 1
        const val ASSERT_EQUALS_OBJ = ERROR + 2
        const val ASSERT_EQUALS_LONG = ERROR + 3
        const val ASSERT_NOT = ERROR + 4
        const val ASSERT_NOT_OBJ = ERROR + 5
        const val ASSERT_NOT_LONG = ERROR + 6
        const val INT_TO_LONG = ERROR + 7
        const val LONG_TO_INT = ERROR + 8
        const val COMPARE = ERROR + 9
    }
}
