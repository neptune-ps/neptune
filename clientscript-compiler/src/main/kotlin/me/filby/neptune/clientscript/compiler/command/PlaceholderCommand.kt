package me.filby.neptune.clientscript.compiler.command

import me.filby.neptune.runescript.compiler.codegen.Opcode
import me.filby.neptune.runescript.compiler.configuration.command.CodeGeneratorContext
import me.filby.neptune.runescript.compiler.configuration.command.DynamicCommandHandler
import me.filby.neptune.runescript.compiler.configuration.command.TypeCheckingContext
import me.filby.neptune.runescript.compiler.type
import me.filby.neptune.runescript.compiler.type.MetaType
import me.filby.neptune.runescript.compiler.type.Type

/**
 * A command that allows replacing a command call with a constant value. The
 * return type is set to [type].
 */
class PlaceholderCommand(private val type: Type, private val value: Any) : DynamicCommandHandler {
    override fun TypeCheckingContext.typeCheck() {
        checkArgumentTypes(MetaType.Unit)
        expression.type = type
    }

    override fun CodeGeneratorContext.generateCode() {
        expression.lineInstruction()
        when (value) {
            is Int -> instruction(Opcode.PushConstantInt, value)
            is String -> instruction(Opcode.PushConstantString, value)
            else -> error("Unsupported value: $value")
        }
    }
}
