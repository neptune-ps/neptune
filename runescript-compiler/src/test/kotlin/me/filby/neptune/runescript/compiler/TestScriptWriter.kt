package me.filby.neptune.runescript.compiler

import me.filby.neptune.runescript.compiler.codegen.Opcode
import me.filby.neptune.runescript.compiler.codegen.script.Label
import me.filby.neptune.runescript.compiler.codegen.script.RuneScript
import me.filby.neptune.runescript.compiler.codegen.script.SwitchTable
import me.filby.neptune.runescript.compiler.runtime.ScriptManager
import me.filby.neptune.runescript.compiler.runtime.TestOpcodes
import me.filby.neptune.runescript.compiler.symbol.LocalVariableSymbol
import me.filby.neptune.runescript.compiler.symbol.ScriptSymbol
import me.filby.neptune.runescript.compiler.symbol.Symbol
import me.filby.neptune.runescript.compiler.type.BaseVarType
import me.filby.neptune.runescript.compiler.writer.BaseScriptWriter
import me.filby.neptune.runescript.runtime.impl.opcodes.BaseCoreOpcodes

internal class TestScriptWriter(private val scriptManager: ScriptManager) :
    BaseScriptWriter<TestScriptWriterContext>() {
    override fun finishWrite(script: RuneScript, context: TestScriptWriterContext) {
        scriptManager.add(context.build())
    }

    override fun createContext(script: RuneScript): TestScriptWriterContext {
        return TestScriptWriterContext(script)
    }

    override fun TestScriptWriterContext.writePushConstantInt(value: Int) {
        instruction(BaseCoreOpcodes.PUSH_CONSTANT_INT, value)
    }

    override fun TestScriptWriterContext.writePushConstantString(value: String) {
        instruction(BaseCoreOpcodes.PUSH_CONSTANT_STRING, value)
    }

    override fun TestScriptWriterContext.writePushConstantLong(value: Long) {
        instruction(BaseCoreOpcodes.PUSH_CONSTANT_LONG, value)
    }

    override fun TestScriptWriterContext.writePushConstantSymbol(value: Symbol) {
        error("identifier references not supported")
    }

    override fun TestScriptWriterContext.writePushVar(symbol: Symbol) {
        if (symbol !is LocalVariableSymbol) {
            error(symbol)
        }

        val id = script.locals.getVariableId(symbol)
        val type = symbol.type.baseType?.ordinal ?: error("unable to determine stack type")
        instruction(BaseCoreOpcodes.PUSH_LOCAL, ((id shl 16) or type))
    }

    override fun TestScriptWriterContext.writePopVar(symbol: Symbol) {
        if (symbol !is LocalVariableSymbol) {
            error(symbol)
        }

        val id = script.locals.getVariableId(symbol)
        val type = symbol.type.baseType?.ordinal ?: error("unable to determine stack type")
        instruction(BaseCoreOpcodes.POP_LOCAL, ((id shl 16) or type))
    }

    override fun TestScriptWriterContext.writeDefineArray(symbol: Symbol) {
        error("not implemented")
    }

    override fun TestScriptWriterContext.writeSwitch(switchTable: SwitchTable) {
        error("not implemented")
    }

    override fun TestScriptWriterContext.writeBranch(branchOpcode: Opcode, label: Label) {
        val runtimeBranchOpcode = BRANCHES[branchOpcode] ?: error(branchOpcode)
        val labelIndex = jumpTable[label] ?: error(label)
        instruction(runtimeBranchOpcode, labelIndex - curIndex - 1)
    }

    override fun TestScriptWriterContext.writeJoinString(count: Int) {
        instruction(BaseCoreOpcodes.JOIN_STRING, count)
    }

    override fun TestScriptWriterContext.writeDiscard(baseType: BaseVarType) {
        instruction(BaseCoreOpcodes.POP_DISCARD, baseType.ordinal)
    }

    override fun TestScriptWriterContext.writeGosub(symbol: ScriptSymbol.ClientScriptSymbol) {
        val name = "[proc,${symbol.name}]"
        val id = scriptManager.findOrGenerateId(name)
        instruction(BaseCoreOpcodes.GOSUB_WITH_PARAMS, id)
    }

    override fun TestScriptWriterContext.writeCommand(symbol: ScriptSymbol.ClientScriptSymbol) {
        val id = COMMANDS[symbol.name] ?: error("unmapped command: ${symbol.name}")
        instruction(id)
    }

    override fun TestScriptWriterContext.writeReturn() {
        instruction(BaseCoreOpcodes.RETURN)
    }

    override fun TestScriptWriterContext.writeMath(opcode: Opcode) {
        val runtimeOpcode = MATH[opcode] ?: error(opcode)
        instruction(runtimeOpcode)
    }

    private companion object {
        private val BRANCHES = mapOf(
            Opcode.BRANCH to BaseCoreOpcodes.BRANCH,
            Opcode.BRANCH_NOT to BaseCoreOpcodes.BRANCH_NOT,
            Opcode.BRANCH_EQUALS to BaseCoreOpcodes.BRANCH_EQUALS,
            Opcode.BRANCH_LESS_THAN to BaseCoreOpcodes.BRANCH_LESS_THAN,
            Opcode.BRANCH_GREATER_THAN to BaseCoreOpcodes.BRANCH_GREATER_THAN,
            Opcode.BRANCH_LESS_THAN_OR_EQUALS to BaseCoreOpcodes.BRANCH_LESS_THAN_OR_EQUALS,
            Opcode.BRANCH_GREATER_THAN_OR_EQUALS to BaseCoreOpcodes.BRANCH_GREATER_THAN_OR_EQUALS,
        )

        private val MATH = mapOf(
            Opcode.ADD to BaseCoreOpcodes.ADD,
            Opcode.SUB to BaseCoreOpcodes.SUB,
            Opcode.MULTIPLY to BaseCoreOpcodes.MULTIPLY,
            Opcode.DIVIDE to BaseCoreOpcodes.DIVIDE,
            Opcode.MODULO to BaseCoreOpcodes.MODULO,
            Opcode.OR to BaseCoreOpcodes.OR,
            Opcode.AND to BaseCoreOpcodes.AND,
        )

        private val COMMANDS = mapOf(
            "tostring" to BaseCoreOpcodes.TOSTRING,
            "tostring_long" to BaseCoreOpcodes.TOSTRING_LONG,
            "string_length" to BaseCoreOpcodes.STRING_LENGTH,
            "substring" to BaseCoreOpcodes.SUBSTRING,
            "string_indexof_string" to BaseCoreOpcodes.STRING_INDEXOF_STRING,
            "append" to BaseCoreOpcodes.APPEND,
            "println" to BaseCoreOpcodes.PRINTLN,
            "error" to TestOpcodes.ERROR,
            "assert_equals" to TestOpcodes.ASSERT_EQUALS,
            "assert_equals_obj" to TestOpcodes.ASSERT_EQUALS_OBJ,
            "assert_equals_long" to TestOpcodes.ASSERT_EQUALS_LONG,
            "assert_not" to TestOpcodes.ASSERT_NOT,
            "assert_not_obj" to TestOpcodes.ASSERT_NOT_OBJ,
            "assert_not_long" to TestOpcodes.ASSERT_NOT_LONG,
        )
    }
}
