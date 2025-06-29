package me.filby.neptune.clientscript.compiler.command

import me.filby.neptune.clientscript.compiler.type.ScriptVarType
import me.filby.neptune.runescript.ast.expr.Identifier
import me.filby.neptune.runescript.compiler.codegen.Opcode
import me.filby.neptune.runescript.compiler.configuration.command.CodeGeneratorContext
import me.filby.neptune.runescript.compiler.configuration.command.DynamicCommandHandler
import me.filby.neptune.runescript.compiler.configuration.command.TypeCheckingContext
import me.filby.neptune.runescript.compiler.type
import me.filby.neptune.runescript.compiler.type.BaseVarType
import me.filby.neptune.runescript.compiler.type.MetaType
import me.filby.neptune.runescript.compiler.type.PrimitiveType
import me.filby.neptune.runescript.compiler.type.TupleType
import me.filby.neptune.runescript.compiler.type.Type

/**
 * A temporary command handler implementation for `if_runscript`. This implementation does
 * not verify any argument types except for the first 3. The first argument is assumed to
 * be a script id, but for now we will just directly pass in an id.
 */
class IfRunScriptCommandHandler : DynamicCommandHandler {
    override fun TypeCheckingContext.typeCheck() {
        checkArgument(0, PrimitiveType.INT)
        val button = checkArgument(1, ScriptVarType.COMPONENT)
        checkArgument(2, PrimitiveType.INT)

        val expectedTypes = arrayListOf<Type>(
            PrimitiveType.INT,
            ScriptVarType.COMPONENT,
            PrimitiveType.INT,
        )

        for (i in 3 until expression.arguments.size) {
            checkArgument(i, MetaType.Any)
            expectedTypes += MetaType.Any
        }

        if (checkArgumentTypes(TupleType.fromList(expectedTypes))) {
            // button shouldn't be null here since we're within the block that check the expected types
            // which requires at least 3 arguments.
            if (button != null && button !is Identifier) {
                // the semantics of this command requires the component to be a static reference to
                // look up the proper script. it isn't technically necessary currently, but we still
                // check for it to make migration easier later on.
                button.reportError(diagnostics, "Component reference must be constant.")
            }
        }

        expression.type = MetaType.Unit
    }

    override fun CodeGeneratorContext.generateCode() {
        val arguments = expression.arguments
        arguments.visit()

        if (arguments.size > 3) {
            val shortTypes = arguments
                .subList(3, arguments.size)
                .map { typeToCharCode(it.type) }
                .joinToString("")
            instruction(Opcode.PushConstantString, shortTypes)
        } else {
            instruction(Opcode.PushConstantString, "")
        }

        command()
    }

    private fun typeToCharCode(type: Type): Char {
        val code = when (type.baseType) {
            BaseVarType.INTEGER -> 'i'
            BaseVarType.STRING -> 's'
            BaseVarType.LONG -> 'l'
            BaseVarType.ARRAY -> type.code
            null -> null
        }
        return code ?: error("Invalid char code for type: ${type.representation}")
    }
}
