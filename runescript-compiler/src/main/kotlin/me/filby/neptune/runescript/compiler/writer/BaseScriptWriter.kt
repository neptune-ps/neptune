package me.filby.neptune.runescript.compiler.writer

import me.filby.neptune.runescript.compiler.codegen.Instruction
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
import me.filby.neptune.runescript.compiler.type.wrapped.ArrayType
import me.filby.neptune.runescript.compiler.writer.BaseScriptWriter.BaseScriptWriterContext
import java.io.Closeable
import java.util.TreeMap

/**
 * A basic implementation of [ScriptWriter] with some utility functions for writing
 * a script.
 */
public abstract class BaseScriptWriter<T : BaseScriptWriterContext>(public val idProvider: IdProvider) : ScriptWriter {
    override fun write(script: RuneScript) {
        createContext(script).use { context ->
            for (block in script.blocks) {
                context.enterBlock(block)

                for (instruction in block.instructions) {
                    // write the current instruction
                    writeInstruction(context, instruction)

                    // update current instruction index
                    context.curIndex++
                }
            }

            finishWrite(script, context)
        }
    }

    protected abstract fun finishWrite(script: RuneScript, context: T)

    /**
     * Creates a new context that is passed to each opcode writer.
     */
    protected abstract fun createContext(script: RuneScript): T

    // Opcode specific write functions

    private fun writeInstruction(context: T, instruction: Instruction<*>) {
        val (opcode, operand) = instruction
        when (opcode) {
            Opcode.PushConstantInt -> context.writePushConstantInt(operand as Int)
            Opcode.PushConstantString -> context.writePushConstantString(operand as String)
            Opcode.PushConstantLong -> context.writePushConstantLong(operand as Long)
            Opcode.PushConstantSymbol -> context.writePushConstantSymbol(operand as Symbol)
            Opcode.PushLocalVar -> context.writePushLocalVar(operand as LocalVariableSymbol)
            Opcode.PopLocalVar -> context.writePopLocalVar(operand as LocalVariableSymbol)
            Opcode.PushVar -> context.writePushVar(operand as BasicSymbol)
            Opcode.PopVar -> context.writePopVar(operand as BasicSymbol)
            Opcode.DefineArray -> context.writeDefineArray(operand as LocalVariableSymbol)
            Opcode.Switch -> context.writeSwitch(operand as SwitchTable)
            Opcode.Branch -> context.writeBranch(opcode, operand as Label)
            Opcode.BranchNot -> context.writeBranch(opcode, operand as Label)
            Opcode.BranchEquals -> context.writeBranch(opcode, operand as Label)
            Opcode.BranchLessThan -> context.writeBranch(opcode, operand as Label)
            Opcode.BranchGreaterThan -> context.writeBranch(opcode, operand as Label)
            Opcode.BranchLessThanOrEquals -> context.writeBranch(opcode, operand as Label)
            Opcode.BranchGreaterThanOrEquals -> context.writeBranch(opcode, operand as Label)
            Opcode.LongBranchNot -> context.writeBranch(opcode, operand as Label)
            Opcode.LongBranchEquals -> context.writeBranch(opcode, operand as Label)
            Opcode.LongBranchLessThan -> context.writeBranch(opcode, operand as Label)
            Opcode.LongBranchGreaterThan -> context.writeBranch(opcode, operand as Label)
            Opcode.LongBranchLessThanOrEquals -> context.writeBranch(opcode, operand as Label)
            Opcode.LongBranchGreaterThanOrEquals -> context.writeBranch(opcode, operand as Label)
            Opcode.ObjBranchNot -> context.writeBranch(opcode, operand as Label)
            Opcode.ObjBranchEquals -> context.writeBranch(opcode, operand as Label)
            Opcode.JoinString -> context.writeJoinString(operand as Int)
            Opcode.Discard -> context.writeDiscard(operand as BaseVarType)
            Opcode.Gosub -> context.writeGosub(operand as ScriptSymbol)
            Opcode.Command -> context.writeCommand(operand as ScriptSymbol)
            Opcode.Return -> context.writeReturn()
            Opcode.Add -> context.writeMath(opcode)
            Opcode.Sub -> context.writeMath(opcode)
            Opcode.Multiply -> context.writeMath(opcode)
            Opcode.Divide -> context.writeMath(opcode)
            Opcode.Modulo -> context.writeMath(opcode)
            Opcode.Or -> context.writeMath(opcode)
            Opcode.And -> context.writeMath(opcode)
            Opcode.LongAdd -> context.writeMath(opcode)
            Opcode.LongSub -> context.writeMath(opcode)
            Opcode.LongMultiply -> context.writeMath(opcode)
            Opcode.LongDivide -> context.writeMath(opcode)
            Opcode.LongModulo -> context.writeMath(opcode)
            Opcode.LongOr -> context.writeMath(opcode)
            Opcode.LongAnd -> context.writeMath(opcode)
            Opcode.LineNumber -> error("linenumber opcode should not exist.")
        }
    }

    protected open fun T.enterBlock(block: Block) {
        error("not implemented")
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

    protected open fun T.writePushLocalVar(symbol: LocalVariableSymbol) {
        error("not implemented")
    }

    protected open fun T.writePopLocalVar(symbol: LocalVariableSymbol) {
        error("not implemented")
    }

    protected open fun T.writePushVar(symbol: BasicSymbol) {
        error("not implemented")
    }

    protected open fun T.writePopVar(symbol: BasicSymbol) {
        error("not implemented")
    }

    protected open fun T.writeDefineArray(symbol: LocalVariableSymbol) {
        error("not implemented")
    }

    protected open fun T.writeSwitch(switchTable: SwitchTable) {
        error("not implemented")
    }

    protected open fun T.writeBranch(opcode: Opcode<*>, label: Label) {
        error("not implemented")
    }

    protected open fun T.writeJoinString(count: Int) {
        error("not implemented")
    }

    protected open fun T.writeDiscard(baseType: BaseVarType) {
        error("not implemented")
    }

    protected open fun T.writeGosub(symbol: ScriptSymbol) {
        error("not implemented")
    }

    protected open fun T.writeCommand(symbol: ScriptSymbol) {
        error("not implemented")
    }

    protected open fun T.writeReturn() {
        error("not implemented")
    }

    protected open fun T.writeMath(opcode: Opcode<*>) {
        error("not implemented")
    }

    public companion object {
        // RuneScript helpers

        /**
         * Returns a mapping of instruction index to line number. This modifies the
         * list of instruction by removing any instruction with an opcode of
         * [Opcode.LineNumber].
         */
        public fun RuneScript.generateLineNumberTable(): TreeMap<Int, Int> {
            val table = TreeMap<Int, Int>()
            var index = 0
            for (block in blocks) {
                val it = block.instructions.iterator()
                while (it.hasNext()) {
                    val instruction = it.next()

                    // check if the instruction is a linenumber
                    if (instruction.opcode == Opcode.LineNumber) {
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
    ) : Closeable {
        public val lineNumberTable: TreeMap<Int, Int> = script.generateLineNumberTable()
        public val jumpTable: Map<Label, Int> = script.generateJumpTable()

        public var curIndex: Int = 0
            internal set

        override fun close() {
        }
    }

    /**
     * A helper that maps [Symbol]s to their id.
     */
    public interface IdProvider {
        /**
         * Takes a [Symbol] and returns an [Int] that represents the symbol for runtime use.
         *
         * It is up to implementation to support id generation if something wasn't originally mapped before.
         *
         * The main symbol types are `ScriptSymbol`, `ConfigSymbol`, and `BasicSymbol`.
         */
        public fun get(symbol: Symbol): Int
    }
}
