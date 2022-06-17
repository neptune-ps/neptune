package me.filby.neptune.runescript.compiler.codegen.script

import me.filby.neptune.runescript.compiler.codegen.Instruction

/**
 * Represents a block of instructions.
 */
public class Block(public val label: Label) {
    /**
     * A __mutable__ list of instructions within this block.
     */
    private val _instructions = mutableListOf<Instruction>()

    /**
     * An __immutable__ list of instructions within this block.
     */
    public val instructions: List<Instruction>
        get() = _instructions

    /**
     * Adds [instruction] to this block.
     */
    public fun add(instruction: Instruction) {
        _instructions += instruction
    }

    /**
     * Shortcut to [add].
     */
    @Suppress("NOTHING_TO_INLINE")
    public inline operator fun plusAssign(instruction: Instruction) {
        add(instruction)
    }
}
