package me.filby.neptune.runescript.compiler.codegen

/**
 * Represents a single code instruction generated by the code generator.
 */
public class Instruction(public val opcode: Opcode, public val operand: Any)
