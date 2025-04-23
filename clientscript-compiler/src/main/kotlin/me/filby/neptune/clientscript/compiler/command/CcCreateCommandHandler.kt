package me.filby.neptune.clientscript.compiler.command

import me.filby.neptune.clientscript.compiler.type.ScriptVarType
import me.filby.neptune.runescript.compiler.codegen.Opcode
import me.filby.neptune.runescript.compiler.configuration.command.CodeGeneratorContext
import me.filby.neptune.runescript.compiler.configuration.command.DynamicCommandHandler
import me.filby.neptune.runescript.compiler.configuration.command.TypeCheckingContext
import me.filby.neptune.runescript.compiler.type
import me.filby.neptune.runescript.compiler.type.MetaType
import me.filby.neptune.runescript.compiler.type.PrimitiveType
import me.filby.neptune.runescript.compiler.type.TupleType

/**
 * Handles `cc_create` having an optional 4th boolean argument for OSRS >= 230.
 */
class CcCreateCommandHandler : DynamicCommandHandler {
    override fun TypeCheckingContext.typeCheck() {
        checkArgument(0, ScriptVarType.COMPONENT) // layer
        checkArgument(1, PrimitiveType.INT) // type
        checkArgument(2, PrimitiveType.INT) // subid
        val requireNew = checkArgument(3, PrimitiveType.BOOLEAN)

        val expectedTypes = mutableListOf(
            ScriptVarType.COMPONENT,
            PrimitiveType.INT,
            PrimitiveType.INT,
        )

        // if a 4th argument is supplied then we want to make sure it's a boolean
        if (requireNew != null) {
            expectedTypes += PrimitiveType.BOOLEAN
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
