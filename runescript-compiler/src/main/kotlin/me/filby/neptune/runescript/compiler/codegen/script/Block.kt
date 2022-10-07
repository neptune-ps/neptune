package me.filby.neptune.runescript.compiler.codegen.script

import me.filby.neptune.runescript.compiler.codegen.Instruction

/**
 * Represents a block of instructions.
 */
public class Block(public val label: Label) {
    /**
     * The list of all [Instruction]s within the block.
     */
    public val instructions: MutableList<Instruction> = mutableListOf()

    /**
     * Adds [instruction] to this block.
     */
    public fun add(instruction: Instruction) {
        instructions += instruction
    }

    /**
     * Shortcut to [add].
     */
    @Suppress("NOTHING_TO_INLINE")
    public inline operator fun plusAssign(instruction: Instruction) {
        add(instruction)
    }
}
