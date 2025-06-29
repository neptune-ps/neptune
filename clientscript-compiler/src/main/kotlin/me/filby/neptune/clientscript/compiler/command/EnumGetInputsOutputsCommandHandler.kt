package me.filby.neptune.clientscript.compiler.command

import me.filby.neptune.clientscript.compiler.type.ScriptVarType
import me.filby.neptune.runescript.compiler.configuration.command.DynamicCommandHandler
import me.filby.neptune.runescript.compiler.configuration.command.TypeCheckingContext
import me.filby.neptune.runescript.compiler.type
import me.filby.neptune.runescript.compiler.type.MetaType
import me.filby.neptune.runescript.compiler.type.TupleType
import me.filby.neptune.runescript.compiler.type.wrapped.ArrayType

/**
 * An implementation of [DynamicCommandHandler] that adds support for type checking
 * the `enum_getinputs` and `enum_getoutputs` commands within scripts.
 *
 * Examples:
 * ```
 * def_intarray $keys = enum_getinputs(int, item_list);
 * def_objarray $values = enum_getoutputs(obj, item_list);
 * ```
 */
class EnumGetInputsOutputsCommandHandler : DynamicCommandHandler {
    override fun TypeCheckingContext.typeCheck() {
        // fetch the arguments
        val typeExpr = checkTypeArgument(0)
        checkArgument(1, ScriptVarType.ENUM)

        val type = (typeExpr?.type as? MetaType.Type)?.inner

        // create the expected type of type,enum
        val expectedTypes = TupleType(
            MetaType.Type(type ?: MetaType.Any),
            ScriptVarType.ENUM,
        )

        // compare the expected types with the actual types
        checkArgumentTypes(expectedTypes)

        // set the command type to the specified output type
        expression.type = if (type != null) ArrayType(type) else MetaType.Error
    }
}
