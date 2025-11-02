package me.filby.neptune.clientscript.compiler.command

import me.filby.neptune.clientscript.compiler.type.IfScriptType
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
        val ifScript = checkArgument(0, IF_SCRIPT_ANY)
        val com = checkArgument(1, ScriptVarType.COMPONENT)
        checkArgument(2, PrimitiveType.INT)

        val expectedTypesList = arrayListOf(
            IF_SCRIPT_ANY,
            ScriptVarType.COMPONENT,
            PrimitiveType.INT,
        )

        val ifScriptExpressionType = ifScript?.type
        if (ifScriptExpressionType is IfScriptType) {
            val types = TupleType.toList(ifScriptExpressionType.inner)
            for ((i, type) in types.withIndex()) {
                checkArgument(3 + i, type)
                expectedTypesList += type
            }
        }

        if (checkArgumentTypes(TupleType.fromList(expectedTypesList))) {
            // button shouldn't be null here since we're within the block that checks the expected types
            // which requires at least 3 arguments.
            if (com != null && com !is Identifier) {
                // the semantics of this command requires the component to be a static reference to
                // look up the proper script. it isn't technically necessary currently, but we still
                // check for it to make migration easier later on.
                // Note: it is currently unknown exactly how the component is involved with the resolution.
                com.reportError(diagnostics, "Component reference must be constant.")
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
            BaseVarType.ARRAY -> '*'
            null -> null
        }
        return code ?: error("Invalid char code for type: ${type.representation}")
    }

    private companion object {
        val IF_SCRIPT_ANY = IfScriptType(MetaType.Any)
    }
}
