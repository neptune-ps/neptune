package me.filby.neptune.runescript.compiler.codegen

/**
 * Represents a single code instruction generated by the code generator.
 */
public class Instruction<T : Any>(public val opcode: Opcode<T>, public val operand: T) {
    public operator fun component1(): Opcode<T> = opcode
    public operator fun component2(): T = operand
}
