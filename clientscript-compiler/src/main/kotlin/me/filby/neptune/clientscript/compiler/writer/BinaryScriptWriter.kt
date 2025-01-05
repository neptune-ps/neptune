package me.filby.neptune.clientscript.compiler.writer

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import me.filby.neptune.clientscript.compiler.ClientScriptOpcode
import me.filby.neptune.runescript.compiler.codegen.Opcode
import me.filby.neptune.runescript.compiler.codegen.script.Block
import me.filby.neptune.runescript.compiler.codegen.script.Label
import me.filby.neptune.runescript.compiler.codegen.script.RuneScript
import me.filby.neptune.runescript.compiler.codegen.script.SwitchTable
import me.filby.neptune.runescript.compiler.symbol.BasicSymbol
import me.filby.neptune.runescript.compiler.symbol.LocalVariableSymbol
import me.filby.neptune.runescript.compiler.symbol.ScriptSymbol
import me.filby.neptune.runescript.compiler.symbol.Symbol
import me.filby.neptune.runescript.compiler.type.BaseVarType
import me.filby.neptune.runescript.compiler.type.MetaType
import me.filby.neptune.runescript.compiler.type.wrapped.ArrayType
import me.filby.neptune.runescript.compiler.type.wrapped.VarBitType
import me.filby.neptune.runescript.compiler.type.wrapped.VarClanSettingsType
import me.filby.neptune.runescript.compiler.type.wrapped.VarClanType
import me.filby.neptune.runescript.compiler.type.wrapped.VarClientType
import me.filby.neptune.runescript.compiler.type.wrapped.VarPlayerType
import me.filby.neptune.runescript.compiler.writer.BaseScriptWriter

/**
 * A `ScriptWriter` implementation that writes to a [ByteBuf]. Implementations
 * must override [outputScript] to handle the binary output for each script.
 */
abstract class BinaryScriptWriter(
    idProvider: IdProvider,
    private val allocator: ByteBufAllocator = ByteBufAllocator.DEFAULT,
) : BaseScriptWriter<BinaryScriptWriterContext>(idProvider) {
    /**
     * Handles the binary output of [script] where [data] is the script in a binary format.
     *
     * The `data` buffer is released automatically after the call to this function.
     */
    abstract fun outputScript(script: RuneScript, data: ByteBuf)

    override fun finishWrite(script: RuneScript, context: BinaryScriptWriterContext) {
        val buffer = context.finish()
        try {
            outputScript(script, buffer)
        } finally {
            buffer.release()
        }
    }

    override fun createContext(script: RuneScript): BinaryScriptWriterContext =
        BinaryScriptWriterContext(script, allocator)

    override fun BinaryScriptWriterContext.enterBlock(block: Block) {
        // NO-OP
    }

    override fun BinaryScriptWriterContext.writePushConstantInt(value: Int) {
        instruction(ClientScriptOpcode.PUSH_CONSTANT_INT, value)
    }

    override fun BinaryScriptWriterContext.writePushConstantString(value: String) {
        instruction(ClientScriptOpcode.PUSH_CONSTANT_STRING, value)
    }

    override fun BinaryScriptWriterContext.writePushConstantLong(value: Long) {
        error("Not supported.")
    }

    override fun BinaryScriptWriterContext.writePushConstantSymbol(value: Symbol) {
        val id = when {
            value is LocalVariableSymbol -> script.locals.getVariableId(value)
            value is BasicSymbol && value.type is MetaType.Type -> {
                val type = value.type as MetaType.Type
                type.inner.code?.code ?: error(type.inner)
            }
            else -> idProvider.get(value)
        }
        instruction(ClientScriptOpcode.PUSH_CONSTANT_INT, id)
    }

    override fun BinaryScriptWriterContext.writePushLocalVar(symbol: LocalVariableSymbol) {
        val id = script.locals.getVariableId(symbol)
        val op = when {
            symbol.type is ArrayType -> ClientScriptOpcode.PUSH_ARRAY_INT
            symbol.type.baseType == BaseVarType.STRING -> ClientScriptOpcode.PUSH_STRING_LOCAL
            symbol.type.baseType == BaseVarType.INTEGER -> ClientScriptOpcode.PUSH_INT_LOCAL
            else -> error(symbol)
        }
        instruction(op, id)
    }

    override fun BinaryScriptWriterContext.writePopLocalVar(symbol: LocalVariableSymbol) {
        val id = script.locals.getVariableId(symbol)
        val op = when {
            symbol.type is ArrayType -> ClientScriptOpcode.POP_ARRAY_INT
            symbol.type.baseType == BaseVarType.STRING -> ClientScriptOpcode.POP_STRING_LOCAL
            symbol.type.baseType == BaseVarType.INTEGER -> ClientScriptOpcode.POP_INT_LOCAL
            else -> error(symbol)
        }
        instruction(op, id)
    }

    override fun BinaryScriptWriterContext.writePushVar(symbol: BasicSymbol) {
        val id = idProvider.get(symbol)
        val opcode = when (val type = symbol.type) {
            is VarPlayerType -> ClientScriptOpcode.PUSH_VARP
            is VarBitType -> ClientScriptOpcode.PUSH_VARBIT
            is VarClientType -> when (type.inner.baseType) {
                BaseVarType.INTEGER -> ClientScriptOpcode.PUSH_VARC_INT
                BaseVarType.STRING -> ClientScriptOpcode.PUSH_VARC_STRING
                else -> error(type.inner)
            }
            is VarClanType -> ClientScriptOpcode.PUSH_VARCLAN
            is VarClanSettingsType -> ClientScriptOpcode.PUSH_VARCLANSETTING
            else -> error(symbol)
        }
        instruction(opcode, id)
    }

    override fun BinaryScriptWriterContext.writePopVar(symbol: BasicSymbol) {
        val id = idProvider.get(symbol)
        val opcode = when (val type = symbol.type) {
            is VarPlayerType -> ClientScriptOpcode.POP_VARP
            is VarBitType -> ClientScriptOpcode.POP_VARBIT
            is VarClientType -> when (type.inner.baseType) {
                BaseVarType.INTEGER -> ClientScriptOpcode.POP_VARC_INT
                BaseVarType.STRING -> ClientScriptOpcode.POP_VARC_STRING
                else -> error(type.inner)
            }
            else -> error(symbol)
        }
        instruction(opcode, id)
    }

    override fun BinaryScriptWriterContext.writeDefineArray(symbol: LocalVariableSymbol) {
        val id = script.locals.getVariableId(symbol)
        val code = (symbol.type as ArrayType).inner.code?.code ?: error("Type has no char code: ${symbol.type}")
        instruction(ClientScriptOpcode.DEFINE_ARRAY, (id shl 16) or code)
    }

    override fun BinaryScriptWriterContext.writeSwitch(switchTable: SwitchTable) {
        switch(switchTable.id) {
            var totalKeyCount = 0
            for ((label, keys) in switchTable.cases) {
                val jumpLocation = jumpTable[label] ?: error("Label not found: $label")
                val relativeJumpLocation = jumpLocation - curIndex - 1
                for (key in keys) {
                    switchCase(findCaseKeyValue(key), relativeJumpLocation)
                }
                totalKeyCount += keys.size
            }
            totalKeyCount
        }
    }

    private fun findCaseKeyValue(key: Any) = when (key) {
        is Int -> key
        is Symbol -> idProvider.get(key)
        else -> error(key)
    }

    override fun BinaryScriptWriterContext.writeBranch(opcode: Opcode<*>, label: Label) {
        val op = when (opcode) {
            Opcode.Branch -> ClientScriptOpcode.BRANCH
            Opcode.BranchNot -> ClientScriptOpcode.BRANCH_NOT
            Opcode.BranchEquals -> ClientScriptOpcode.BRANCH_EQUALS
            Opcode.BranchLessThan -> ClientScriptOpcode.BRANCH_LESS_THAN
            Opcode.BranchGreaterThan -> ClientScriptOpcode.BRANCH_GREATER_THAN
            Opcode.BranchLessThanOrEquals -> ClientScriptOpcode.BRANCH_LESS_THAN_OR_EQUALS
            Opcode.BranchGreaterThanOrEquals -> ClientScriptOpcode.BRANCH_GREATER_THAN_OR_EQUALS
            else -> error(opcode)
        }
        val jumpLocation = jumpTable[label] ?: error("Label not found: $label")
        instruction(op, jumpLocation - curIndex - 1)
    }

    override fun BinaryScriptWriterContext.writeJoinString(count: Int) {
        instruction(ClientScriptOpcode.JOIN_STRING, count)
    }

    override fun BinaryScriptWriterContext.writeDiscard(baseType: BaseVarType) {
        val op = when (baseType) {
            BaseVarType.INTEGER -> ClientScriptOpcode.POP_INT_DISCARD
            BaseVarType.STRING -> ClientScriptOpcode.POP_STRING_DISCARD
            else -> error(baseType)
        }
        instruction(op, 0)
    }

    override fun BinaryScriptWriterContext.writeGosub(symbol: ScriptSymbol) {
        val id = idProvider.get(symbol)
        instruction(ClientScriptOpcode.GOSUB_WITH_PARAMS, id)
    }

    override fun BinaryScriptWriterContext.writeJump(symbol: ScriptSymbol) {
        error("Not supported.")
    }

    override fun BinaryScriptWriterContext.writeCommand(symbol: ScriptSymbol) {
        val op = idProvider.get(symbol)
        val secondary = symbol.name.startsWith(".")
        instruction(op, if (secondary) 1 else 0)
    }

    override fun BinaryScriptWriterContext.writeReturn() {
        instruction(ClientScriptOpcode.RETURN, 0)
    }

    override fun BinaryScriptWriterContext.writeMath(opcode: Opcode<*>) {
        val op = when (opcode) {
            Opcode.Add -> ClientScriptOpcode.ADD
            Opcode.Sub -> ClientScriptOpcode.SUB
            Opcode.Multiply -> ClientScriptOpcode.MULTIPLY
            Opcode.Divide -> ClientScriptOpcode.DIVIDE
            Opcode.Modulo -> ClientScriptOpcode.MODULO
            Opcode.Or -> ClientScriptOpcode.OR
            Opcode.And -> ClientScriptOpcode.AND
            else -> error(opcode)
        }
        instruction(op, 0)
    }
}
