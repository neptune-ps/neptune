package me.filby.neptune.runescript.compiler.codegen.script.cfg

import me.filby.neptune.runescript.compiler.pointer.PointerType

public class PointerInstructionNode(public val set: Set<PointerType>) : InstructionNode(null)
