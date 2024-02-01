package me.filby.neptune.runescript.compiler.codegen.script.cfg

import me.filby.neptune.runescript.compiler.codegen.Instruction
import me.filby.neptune.runescript.compiler.codegen.Opcode
import me.filby.neptune.runescript.compiler.codegen.script.Block
import me.filby.neptune.runescript.compiler.codegen.script.Label
import me.filby.neptune.runescript.compiler.codegen.script.SwitchTable
import me.filby.neptune.runescript.compiler.pointer.PointerHolder
import me.filby.neptune.runescript.compiler.symbol.ScriptSymbol

public class GraphGenerator(private val commandPointers: Map<String, PointerHolder>) {
    public fun generate(blocks: List<Block>): List<InstructionNode> {
        val nodeCache = mutableMapOf<Instruction<*>, InstructionNode>()
        val nodes = mutableListOf<InstructionNode>()

        val start = InstructionNode(null)
        start.addNext(blocks.first().firstInstruction(nodeCache, blocks))
        nodes += start

        var potentialConditionalPointer = false
        for ((blockIdx, block) in blocks.withIndex()) {
            for ((instructionIdx, instruction) in block.instructions.withIndex()) {
                if (instruction.opcode == Opcode.LineNumber) {
                    // TODO generalize
                    continue
                }

                val node = nodeCache.getOrCreate(instruction)
                nodes += node

                // TODO should exclude commands that return 'nothing' also
                if (instruction.opcode !in TERMINAL_OPCODES) {
                    val next = if (instructionIdx + 1 < block.instructions.size) {
                        block.instructions[instructionIdx + 1]
                    } else if (blockIdx + 1 < blocks.size) {
                        blocks[blockIdx + 1].instructions.first()
                    } else {
                        error("")
                    }

                    node.addNext(nodeCache.getOrCreate(next))
                }

                if (potentialConditionalPointer &&
                    instruction.opcode == Opcode.BranchEquals &&
                    checkConditional(block.instructions, instructionIdx)
                ) {
                    // TODO support for inverted cases (x = false, x ! false)

                    val label = instruction.operand as Label
                    val jumpBlock = blocks.find { it.label == label } ?: error("Unable to find block.")

                    val commandNode = block.instructions[instructionIdx - 2]
                    if (commandNode.opcode != Opcode.Command) {
                        error("...")
                    }

                    val commandName = (commandNode.operand as ScriptSymbol).name
                    val pointers = commandPointers[commandName]!!.set
                    val setPointerNode = PointerInstructionNode(pointers)
                    nodes += setPointerNode

                    node.addNext(setPointerNode)
                    setPointerNode.addNext(jumpBlock.firstInstruction(nodeCache, blocks))

                    potentialConditionalPointer = false
                } else if (instruction.opcode in BRANCH_OPCODES) {
                    val label = instruction.operand as Label
                    val jumpBlock = blocks.find { it.label == label } ?: error("Unable to find block.")
                    node.addNext(jumpBlock.firstInstruction(nodeCache, blocks))
                } else if (instruction.opcode == Opcode.Switch) {
                    val table = instruction.operand as SwitchTable
                    for (case in table.cases) {
                        if (case.keys.isEmpty()) {
                            continue
                        }

                        val label = case.label
                        val jumpBlock = blocks.find { it.label == label } ?: error("Unable to find block.")
                        node.addNext(jumpBlock.firstInstruction(nodeCache, blocks))
                    }
                }

                if (instruction.isConditionalPointerSetter()) {
                    potentialConditionalPointer = true
                }
            }
        }

        return nodes
    }

    private fun checkConditional(instructions: List<Instruction<*>>, instructionIdx: Int): Boolean {
        if (instructionIdx < 2) {
            return false
        }

        val inst1 = instructions[instructionIdx - 2]
        if (!inst1.isConditionalPointerSetter()) {
            return false
        }

        val in2 = instructions[instructionIdx - 1]
        if (in2.opcode != Opcode.PushConstantInt || in2.operand != 1) {
            return false
        }
        return true
    }

    private fun Instruction<*>.isConditionalPointerSetter(): Boolean {
        if (opcode == Opcode.Command && operand is ScriptSymbol) {
            val commandName = operand.name
            val pointers = commandPointers[commandName]
            return pointers != null && pointers.conditionalSet
        }
        return false
    }

    private fun Block.firstInstruction(
        cache: MutableMap<Instruction<*>, InstructionNode>,
        blocks: List<Block>,
    ): InstructionNode {
        if (instructions.isNotEmpty()) {
            val instruction = instructions.firstValid()
            if (instruction != null) {
                return cache.getOrCreate(instruction)
            }
        }

        val startBlock = blocks.indexOf(this)
        for (i in startBlock until blocks.size) {
            val block = blocks[i]
            val instruction = block.instructions.firstValid()
            if (instruction != null) {
                return cache.getOrCreate(instruction)
            }
        }

        error("No instructions remaining.")
    }

    private fun List<Instruction<*>>.firstValid(): Instruction<*>? {
        for (instruction in this) {
            if (instruction.opcode != Opcode.LineNumber) {
                return instruction
            }
        }
        return null
    }

    private fun MutableMap<Instruction<*>, InstructionNode>.getOrCreate(instruction: Instruction<*>): InstructionNode {
        return computeIfAbsent(instruction) { InstructionNode(it) }
    }

    private companion object {
        private val TERMINAL_OPCODES = setOf(Opcode.Branch, Opcode.Jump, Opcode.Return)

        private val BRANCH_OPCODES = setOf(
            Opcode.Branch,
            Opcode.BranchNot,
            Opcode.BranchEquals,
            Opcode.BranchLessThan,
            Opcode.BranchGreaterThan,
            Opcode.BranchLessThanOrEquals,
            Opcode.BranchGreaterThanOrEquals,
            Opcode.LongBranchNot,
            Opcode.LongBranchEquals,
            Opcode.LongBranchLessThan,
            Opcode.LongBranchGreaterThan,
            Opcode.LongBranchLessThanOrEquals,
            Opcode.LongBranchGreaterThanOrEquals,
            Opcode.ObjBranchNot,
            Opcode.ObjBranchEquals,
        )
    }
}
