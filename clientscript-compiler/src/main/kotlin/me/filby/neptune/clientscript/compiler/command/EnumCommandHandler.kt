package me.filby.neptune.clientscript.compiler.command

import me.filby.neptune.clientscript.compiler.type.ScriptVarType
import me.filby.neptune.runescript.compiler.configuration.command.DynamicCommandHandler
import me.filby.neptune.runescript.compiler.configuration.command.TypeCheckingContext
import me.filby.neptune.runescript.compiler.type
import me.filby.neptune.runescript.compiler.type.MetaType
import me.filby.neptune.runescript.compiler.type.TupleType

/**
 * An implementation of [DynamicCommandHandler] that adds support for type checking
 * the `enum` command within scripts.
 *
 * Example:
 * ```
 * def_obj $item = enum(int, obj, item_list, $index);
 * ```
 */
class EnumCommandHandler : DynamicCommandHandler {
    override fun TypeCheckingContext.typeCheck() {
        // fetch the arguments (minus last)
        val inputTypeExpr = checkTypeArgument(0)
        val outputTypeExpr = checkTypeArgument(1)
        checkArgument(2, ScriptVarType.ENUM)

        // fetch the evaluation of the input and output types
        val inputType = (inputTypeExpr?.type as? MetaType.Type)?.inner
        val outputType = (outputTypeExpr?.type as? MetaType.Type)?.inner

        // type hint last argument with the inputtype inner type
        checkArgument(3, inputType)

        // create the expected type of type,type,enum,any
        val expectedTypes = TupleType(
            MetaType.Type(inputType ?: MetaType.Any),
            MetaType.Type(outputType ?: MetaType.Any),
            ScriptVarType.ENUM,
            inputType ?: MetaType.Any,
        )

        // compare the expected types with the actual types
        checkArgumentTypes(expectedTypes)

        // set the command type to the specified output type
        expression.type = outputType ?: MetaType.Error
    }
}
