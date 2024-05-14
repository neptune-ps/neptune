package me.filby.neptune.clientscript.compiler.command.debug

import me.filby.neptune.runescript.ast.Script
import me.filby.neptune.runescript.compiler.codegen.Opcode
import me.filby.neptune.runescript.compiler.configuration.command.CodeGeneratorContext
import me.filby.neptune.runescript.compiler.configuration.command.DynamicCommandHandler
import me.filby.neptune.runescript.compiler.configuration.command.TypeCheckingContext
import me.filby.neptune.runescript.compiler.type
import me.filby.neptune.runescript.compiler.type.MetaType
import me.filby.neptune.runescript.compiler.type.PrimitiveType

/**
 * Dynamic command handler that replaces the call with a string constant containing
 * the name of the script it is called in.
 */
class ScriptCommandHandler : DynamicCommandHandler {
    override fun TypeCheckingContext.typeCheck() {
        checkArgumentTypes(MetaType.Unit)
        expression.type = PrimitiveType.STRING
    }

    override fun CodeGeneratorContext.generateCode() {
        val script = checkNotNull(expression.findParentByType<Script>())
        val name = "[${script.trigger.text},${script.name.text}]"

        expression.lineInstruction()
        instruction(Opcode.PushConstantString, name)
    }
}
