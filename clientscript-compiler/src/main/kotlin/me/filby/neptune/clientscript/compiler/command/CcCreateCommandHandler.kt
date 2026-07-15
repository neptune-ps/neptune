package me.filby.neptune.clientscript.compiler.command

import me.filby.neptune.clientscript.compiler.type.ScriptVarType
import me.filby.neptune.runescript.compiler.codegen.Opcode
import me.filby.neptune.runescript.compiler.configuration.command.CodeGeneratorContext
import me.filby.neptune.runescript.compiler.configuration.command.DynamicCommandHandler
import me.filby.neptune.runescript.compiler.configuration.command.TypeCheckingContext
import me.filby.neptune.runescript.compiler.type
import me.filby.neptune.runescript.compiler.type.MetaType
import me.filby.neptune.runescript.compiler.type.TupleType

/**
 * Handles `cc_create` having an optional 4th boolean argument for OSRS >= 230.
 */
class CcCreateCommandHandler : DynamicCommandHandler {
    override fun TypeCheckingContext.typeCheck() {
        checkArgument(0, ScriptVarType.COMPONENT) // layer
        checkArgument(1, ScriptVarType.INT) // type
        checkArgument(2, ScriptVarType.INT) // subid
        val requireNew = checkArgument(3, ScriptVarType.BOOLEAN)

        val expectedTypes = mutableListOf(
            ScriptVarType.COMPONENT,
            ScriptVarType.INT,
            ScriptVarType.INT,
        )

        // if a 4th argument is supplied then we want to make sure it's a boolean
        if (requireNew != null) {
            expectedTypes += ScriptVarType.BOOLEAN
        }

        checkArgumentTypes(TupleType.fromList(expectedTypes))
        expression.type = MetaType.Unit
    }

    override fun CodeGeneratorContext.generateCode() {
        expression.arguments.visit()
        if (expression.arguments.size == 3) {
            // optional boolean argument, default to false
            instruction(Opcode.PushConstantInt, 0)
        }
        command()
    }
}
