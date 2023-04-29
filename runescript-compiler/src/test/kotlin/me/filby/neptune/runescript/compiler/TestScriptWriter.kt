package me.filby.neptune.runescript.compiler

import me.filby.neptune.runescript.compiler.codegen.Opcode
import me.filby.neptune.runescript.compiler.codegen.script.Block
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

internal class TestScriptWriter(
    private val scriptManager: ScriptManager
) : BaseScriptWriter<TestScriptWriterContext>(scriptManager) {
    override fun finishWrite(script: RuneScript, context: TestScriptWriterContext) {
        scriptManager.add(context.build())
    }

    override fun createContext(script: RuneScript): TestScriptWriterContext {
        return TestScriptWriterContext(script)
    }

    override fun TestScriptWriterContext.enterBlock(block: Block) {
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
        instruction(BaseCoreOpcodes.PUSH_CONSTANT_INT, idProvider.get(value))
    }

    override fun TestScriptWriterContext.writePushLocalVar(symbol: LocalVariableSymbol) {
        val id = script.locals.getVariableId(symbol)
        val type = symbol.type.baseType?.ordinal ?: error("unable to determine stack type")
        instruction(BaseCoreOpcodes.PUSH_LOCAL, ((id shl 16) or type))
    }

    override fun TestScriptWriterContext.writePopLocalVar(symbol: LocalVariableSymbol) {
        val id = script.locals.getVariableId(symbol)
        val type = symbol.type.baseType?.ordinal ?: error("unable to determine stack type")
        instruction(BaseCoreOpcodes.POP_LOCAL, ((id shl 16) or type))
    }

    override fun TestScriptWriterContext.writeDefineArray(symbol: LocalVariableSymbol) {
        error("not implemented")
    }

    override fun TestScriptWriterContext.writeSwitch(switchTable: SwitchTable) {
        error("not implemented")
    }

    override fun TestScriptWriterContext.writeBranch(opcode: Opcode<*>, label: Label) {
        val runtimeBranchOpcode = BRANCHES[opcode] ?: error(opcode)
        val labelIndex = jumpTable[label] ?: error(label)
        instruction(runtimeBranchOpcode, labelIndex - curIndex - 1)
    }

    override fun TestScriptWriterContext.writeJoinString(count: Int) {
        instruction(BaseCoreOpcodes.JOIN_STRING, count)
    }

    override fun TestScriptWriterContext.writeDiscard(baseType: BaseVarType) {
        instruction(BaseCoreOpcodes.POP_DISCARD, baseType.ordinal)
    }

    override fun TestScriptWriterContext.writeGosub(symbol: ScriptSymbol) {
        val id = idProvider.get(symbol)
        instruction(BaseCoreOpcodes.GOSUB_WITH_PARAMS, id)
    }

    override fun TestScriptWriterContext.writeJump(symbol: ScriptSymbol) {
        val id = idProvider.get(symbol)
        instruction(BaseCoreOpcodes.JUMP_WITH_PARAMS, id)
    }

    override fun TestScriptWriterContext.writeCommand(symbol: ScriptSymbol) {
        val id = COMMANDS[symbol.name] ?: error("unmapped command: ${symbol.name}")
        instruction(id)
    }

    override fun TestScriptWriterContext.writeReturn() {
        instruction(BaseCoreOpcodes.RETURN)
    }

    override fun TestScriptWriterContext.writeMath(opcode: Opcode<*>) {
        val runtimeOpcode = MATH[opcode] ?: error(opcode)
        instruction(runtimeOpcode)
    }

    private companion object {
        private val BRANCHES = mapOf(
            Opcode.Branch to BaseCoreOpcodes.BRANCH,
            Opcode.BranchNot to BaseCoreOpcodes.BRANCH_NOT,
            Opcode.BranchEquals to BaseCoreOpcodes.BRANCH_EQUALS,
            Opcode.BranchLessThan to BaseCoreOpcodes.BRANCH_LESS_THAN,
            Opcode.BranchGreaterThan to BaseCoreOpcodes.BRANCH_GREATER_THAN,
            Opcode.BranchLessThanOrEquals to BaseCoreOpcodes.BRANCH_LESS_THAN_OR_EQUALS,
            Opcode.BranchGreaterThanOrEquals to BaseCoreOpcodes.BRANCH_GREATER_THAN_OR_EQUALS,
            Opcode.LongBranchNot to BaseCoreOpcodes.LONG_BRANCH_NOT,
            Opcode.LongBranchEquals to BaseCoreOpcodes.LONG_BRANCH_EQUALS,
            Opcode.LongBranchLessThan to BaseCoreOpcodes.LONG_BRANCH_LESS_THAN,
            Opcode.LongBranchGreaterThan to BaseCoreOpcodes.LONG_BRANCH_GREATER_THAN,
            Opcode.LongBranchLessThanOrEquals to BaseCoreOpcodes.LONG_BRANCH_LESS_THAN_OR_EQUALS,
            Opcode.LongBranchGreaterThanOrEquals to BaseCoreOpcodes.LONG_BRANCH_GREATER_THAN_OR_EQUALS,
            Opcode.ObjBranchNot to BaseCoreOpcodes.OBJ_BRANCH_NOT,
            Opcode.ObjBranchEquals to BaseCoreOpcodes.OBJ_BRANCH_EQUALS,
        )

        private val MATH = mapOf(
            Opcode.Add to BaseCoreOpcodes.ADD,
            Opcode.Sub to BaseCoreOpcodes.SUB,
            Opcode.Multiply to BaseCoreOpcodes.MULTIPLY,
            Opcode.Divide to BaseCoreOpcodes.DIVIDE,
            Opcode.Modulo to BaseCoreOpcodes.MODULO,
            Opcode.Or to BaseCoreOpcodes.OR,
            Opcode.And to BaseCoreOpcodes.AND,
            Opcode.LongAdd to BaseCoreOpcodes.LONG_ADD,
            Opcode.LongSub to BaseCoreOpcodes.LONG_SUB,
            Opcode.LongMultiply to BaseCoreOpcodes.LONG_MULTIPLY,
            Opcode.LongDivide to BaseCoreOpcodes.LONG_DIVIDE,
            Opcode.LongModulo to BaseCoreOpcodes.LONG_MODULO,
            Opcode.LongOr to BaseCoreOpcodes.LONG_OR,
            Opcode.LongAnd to BaseCoreOpcodes.LONG_AND,
        )

        private val COMMANDS = mapOf(
            "jump" to BaseCoreOpcodes.JUMP,
            "gosub" to BaseCoreOpcodes.GOSUB,
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
            "int_to_long" to TestOpcodes.INT_TO_LONG,
            "long_to_int" to TestOpcodes.LONG_TO_INT,
        )
    }
}
