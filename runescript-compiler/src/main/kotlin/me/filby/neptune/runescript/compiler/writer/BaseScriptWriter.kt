package me.filby.neptune.runescript.compiler.writer

import me.filby.neptune.runescript.compiler.codegen.Instruction
import me.filby.neptune.runescript.compiler.codegen.Opcode
import me.filby.neptune.runescript.compiler.codegen.script.Label
import me.filby.neptune.runescript.compiler.codegen.script.RuneScript
import me.filby.neptune.runescript.compiler.codegen.script.SwitchTable
import me.filby.neptune.runescript.compiler.symbol.LocalVariableSymbol
import me.filby.neptune.runescript.compiler.symbol.ScriptSymbol.ClientScriptSymbol
import me.filby.neptune.runescript.compiler.symbol.Symbol
import me.filby.neptune.runescript.compiler.type.BaseVarType
import me.filby.neptune.runescript.compiler.type.wrapped.ArrayType
import me.filby.neptune.runescript.compiler.writer.BaseScriptWriter.BaseScriptWriterContext
import java.util.TreeMap

/**
 * A basic implementation of [ScriptWriter] with some utility functions for writing
 * a script.
 */
public abstract class BaseScriptWriter<T : BaseScriptWriterContext> : ScriptWriter {
    override fun write(script: RuneScript) {
        val context = createContext(script)

        for (block in script.blocks) {
            for (instruction in block.instructions) {
                // write the current instruction
                writeInstruction(context, instruction)

                // update current instruction index
                context.curIndex++
            }
        }

        finishWrite(script, context)
    }

    protected abstract fun finishWrite(script: RuneScript, context: T)

    /**
     * Creates a new context that is passed to each opcode writer.
     */
    protected abstract fun createContext(script: RuneScript): T

    // Opcode specific write functions

    private fun writeInstruction(context: T, instruction: Instruction) {
        val (opcode, operand) = instruction
        when (opcode) {
            Opcode.PUSH_CONSTANT -> when (operand) {
                is Int -> context.writePushConstantInt(operand)
                is String -> context.writePushConstantString(operand)
                is Long -> context.writePushConstantLong(operand)
                is Symbol -> context.writePushConstantSymbol(operand)
                else -> error("Unsupported push_constant operand type: $operand")
            }

            Opcode.PUSH_VAR -> context.writePushVar(operand as Symbol)
            Opcode.POP_VAR -> context.writePopVar(operand as Symbol)
            Opcode.DEFINE_ARRAY -> context.writeDefineArray(operand as Symbol)
            Opcode.SWITCH -> context.writeSwitch(operand as SwitchTable)
            Opcode.BRANCH -> context.writeBranch(opcode, operand as Label)
            Opcode.BRANCH_NOT -> context.writeBranch(opcode, operand as Label)
            Opcode.BRANCH_EQUALS -> context.writeBranch(opcode, operand as Label)
            Opcode.BRANCH_LESS_THAN -> context.writeBranch(opcode, operand as Label)
            Opcode.BRANCH_GREATER_THAN -> context.writeBranch(opcode, operand as Label)
            Opcode.BRANCH_LESS_THAN_OR_EQUALS -> context.writeBranch(opcode, operand as Label)
            Opcode.BRANCH_GREATER_THAN_OR_EQUALS -> context.writeBranch(opcode, operand as Label)
            Opcode.LONG_BRANCH_NOT -> context.writeBranch(opcode, operand as Label)
            Opcode.LONG_BRANCH_EQUALS -> context.writeBranch(opcode, operand as Label)
            Opcode.LONG_BRANCH_LESS_THAN -> context.writeBranch(opcode, operand as Label)
            Opcode.LONG_BRANCH_GREATER_THAN -> context.writeBranch(opcode, operand as Label)
            Opcode.LONG_BRANCH_LESS_THAN_OR_EQUALS -> context.writeBranch(opcode, operand as Label)
            Opcode.LONG_BRANCH_GREATER_THAN_OR_EQUALS -> context.writeBranch(opcode, operand as Label)
            Opcode.OBJ_BRANCH_NOT -> context.writeBranch(opcode, operand as Label)
            Opcode.OBJ_BRANCH_EQUALS -> context.writeBranch(opcode, operand as Label)
            Opcode.JOIN_STRING -> context.writeJoinString(operand as Int)
            Opcode.DISCARD -> context.writeDiscard(operand as BaseVarType)
            Opcode.GOSUB -> context.writeGosub(operand as ClientScriptSymbol)
            Opcode.COMMAND -> context.writeCommand(operand as ClientScriptSymbol)
            Opcode.RETURN -> context.writeReturn()
            Opcode.ADD -> context.writeMath(opcode)
            Opcode.SUB -> context.writeMath(opcode)
            Opcode.MULTIPLY -> context.writeMath(opcode)
            Opcode.DIVIDE -> context.writeMath(opcode)
            Opcode.MODULO -> context.writeMath(opcode)
            Opcode.OR -> context.writeMath(opcode)
            Opcode.AND -> context.writeMath(opcode)
            Opcode.LONG_ADD -> context.writeMath(opcode)
            Opcode.LONG_SUB -> context.writeMath(opcode)
            Opcode.LONG_MULTIPLY -> context.writeMath(opcode)
            Opcode.LONG_DIVIDE -> context.writeMath(opcode)
            Opcode.LONG_MODULO -> context.writeMath(opcode)
            Opcode.LONG_OR -> context.writeMath(opcode)
            Opcode.LONG_AND -> context.writeMath(opcode)
            Opcode.LINENUMBER -> error("linenumber opcode should not exist.")
        }
    }

    protected open fun T.writePushConstantInt(value: Int) {
        error("not implemented")
    }

    protected open fun T.writePushConstantString(value: String) {
        error("not implemented")
    }

    protected open fun T.writePushConstantLong(value: Long) {
        error("not implemented")
    }

    protected open fun T.writePushConstantSymbol(value: Symbol) {
        error("not implemented")
    }

    protected open fun T.writePushVar(symbol: Symbol) {
        error("not implemented")
    }

    protected open fun T.writePopVar(symbol: Symbol) {
        error("not implemented")
    }

    protected open fun T.writeDefineArray(symbol: Symbol) {
        error("not implemented")
    }

    protected open fun T.writeSwitch(switchTable: SwitchTable) {
        error("not implemented")
    }

    protected open fun T.writeBranch(branchOpcode: Opcode, label: Label) {
        error("not implemented")
    }

    protected open fun T.writeJoinString(count: Int) {
        error("not implemented")
    }

    protected open fun T.writeDiscard(baseType: BaseVarType) {
        error("not implemented")
    }

    protected open fun T.writeGosub(symbol: ClientScriptSymbol) {
        error("not implemented")
    }

    protected open fun T.writeCommand(symbol: ClientScriptSymbol) {
        error("not implemented")
    }

    protected open fun T.writeReturn() {
        error("not implemented")
    }

    protected open fun T.writeMath(opcode: Opcode) {
        error("not implemented")
    }

    public companion object {
        // RuneScript helpers

        /**
         * Returns a mapping of instruction index to line number. This modifies the
         * list of instruction by removing any instruction with an opcode of
         * [Opcode.LINENUMBER].
         */
        public fun RuneScript.generateLineNumberTable(): TreeMap<Int, Int> {
            val table = TreeMap<Int, Int>()
            var index = 0
            for (block in blocks) {
                val it = block.instructions.iterator()
                while (it.hasNext()) {
                    val instruction = it.next()

                    // check if the instruction is a linenumber
                    if (instruction.opcode == Opcode.LINENUMBER) {
                        // add it to the table
                        table[index] = instruction.operand as Int

                        // remove it from instructions
                        it.remove()
                        continue
                    }

                    // increment the instruction index
                    index++
                }
            }
            return table
        }

        /**
         * Returns a mapping of where all [Label]s are located.
         *
         * **WARNING**: This should be called **after** all other things that modify
         * the instruction lists.
         */
        public fun RuneScript.generateJumpTable(): Map<Label, Int> {
            val table = mutableMapOf<Label, Int>()
            var index = 0
            for (block in blocks) {
                table[block.label] = index
                index += block.instructions.size
            }
            return table
        }

        // RuneScript.LocalTable helpers

        /**
         * Returns the total number of parameters with a base var type of [baseType].
         */
        public fun RuneScript.LocalTable.getParameterCount(baseType: BaseVarType): Int {
            return parameters.count { it.type.baseType == baseType }
        }

        /**
         * Returns the total number of local variables with a base var type of [baseType].
         */
        public fun RuneScript.LocalTable.getLocalCount(baseType: BaseVarType): Int {
            return all.count { it.type.baseType == baseType }
        }

        /**
         * Finds the unique identifier for the given [local] variable.
         */
        public fun RuneScript.LocalTable.getVariableId(local: LocalVariableSymbol): Int {
            val isArray = local.type is ArrayType
            return all.asSequence()
                .filter { isArray && it.type is ArrayType || !isArray && it.type.baseType == local.type.baseType }
                .indexOf(local)
        }
    }

    /**
     * A base class passed that contains the context of the [ScriptWriter] when writing
     * specific instructions.
     */
    public open class BaseScriptWriterContext(
        public val script: RuneScript,
    ) {
        public val lineNumberTable: TreeMap<Int, Int> = script.generateLineNumberTable()
        public val jumpTable: Map<Label, Int> = script.generateJumpTable()

        public var curIndex: Int = 0
            internal set
    }
}
