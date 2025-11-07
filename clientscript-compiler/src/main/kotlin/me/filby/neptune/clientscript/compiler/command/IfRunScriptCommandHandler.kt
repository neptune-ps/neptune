package me.filby.neptune.clientscript.compiler.command

import me.filby.neptune.clientscript.compiler.type.IfScriptType
import me.filby.neptune.clientscript.compiler.type.ScriptVarType
import me.filby.neptune.runescript.ast.expr.CommandCallExpression
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
 * Handles the `if_runscript*` command. This command accepts a secondary set of arguments
 * that are forwarded to the server and used within the invoked script.
 *
 * The component argument must be known at compile time. The script lookup is performed by
 * looking up a [IfScriptType], which defines the expected argument types required to invoke
 * the target script.
 *
 * Example:
 * ```
 * if_runscript*(some_script, inter:comp, null)(true)
 * ```
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

        val ifScriptExpressionType = ifScript?.type
        if (ifScriptExpressionType is IfScriptType) {
            val types = TupleType.toList(ifScriptExpressionType.inner)
            for ((i, type) in types.withIndex()) {
                checkArgument(i, type, args2 = true)
            }
            checkArgumentTypes(ifScriptExpressionType.inner, args2 = true)
        }

        expression.type = MetaType.Unit
    }

    override fun CodeGeneratorContext.generateCode() {
        val call = expression as CommandCallExpression

        val arguments = call.arguments
        arguments.visit()

        val arguments2 = call.arguments2
        arguments2.visit()

        if (arguments2 != null) {
            val shortTypes = arguments2
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
