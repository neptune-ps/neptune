package me.filby.neptune.runescript.compiler.writer

import me.filby.neptune.runescript.compiler.codegen.script.RuneScript
import me.filby.neptune.runescript.compiler.symbol.LocalVariableSymbol
import me.filby.neptune.runescript.compiler.type.BaseVarType
import me.filby.neptune.runescript.compiler.type.wrapped.ArrayType

/**
 * A basic implementation of [ScriptWriter] with some utility functions for writing
 * a script.
 */
public abstract class BaseScriptWriter : ScriptWriter {
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
