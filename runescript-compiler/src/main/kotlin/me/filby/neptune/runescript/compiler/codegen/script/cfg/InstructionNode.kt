package me.filby.neptune.runescript.compiler.codegen.script.cfg

import me.filby.neptune.runescript.compiler.codegen.Instruction

public open class InstructionNode(public val instruction: Instruction<*>?) {
    public val next: MutableList<InstructionNode> = mutableListOf()
    public val previous: MutableList<InstructionNode> = mutableListOf()

    public fun addNext(node: InstructionNode) {
        next.add(node)
        node.previous.add(this)
    }

    override fun toString(): String {
        return "InstructionNode(instruction=$instruction)"
    }
}
