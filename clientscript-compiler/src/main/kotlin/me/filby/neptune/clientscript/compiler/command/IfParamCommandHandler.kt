package me.filby.neptune.clientscript.compiler.command

import me.filby.neptune.clientscript.compiler.type.ParamType
import me.filby.neptune.clientscript.compiler.type.ScriptVarType
import me.filby.neptune.runescript.compiler.configuration.command.DynamicCommandHandler
import me.filby.neptune.runescript.compiler.configuration.command.TypeCheckingContext
import me.filby.neptune.runescript.compiler.type
import me.filby.neptune.runescript.compiler.type.MetaType
import me.filby.neptune.runescript.compiler.type.PrimitiveType
import me.filby.neptune.runescript.compiler.type.TupleType

/**
 * An implementation of [DynamicCommandHandler] that adds support for type checking
 * the `if_param` and `cc_param` commands within scripts.
 *
 * Example:
 * ```
 * def_int $i = if_param(some_int_param, $comp, null);
 * def_int $j = cc_param(some_int_param);
 * ```
 */
class IfParamCommandHandler(private val cc: Boolean) : DynamicCommandHandler {
    override fun TypeCheckingContext.typeCheck() {
        // check param reference
        val paramExpr = checkArgument(0, ParamCommandHandler.PARAM_ANY)
        val paramReturnType = (paramExpr?.type as? ParamType)?.inner
        if (!cc) {
            checkArgument(1, ScriptVarType.COMPONENT)
            checkArgument(2, PrimitiveType.INT)
        }

        // define the expected types based on what is currently known
        val expectedTypes = if (!cc) {
            IF_BASE_EXPECTED_TYPES
        } else {
            CC_BASE_EXPECTED_TYPES
        }

        // compare the expected types with the actual types
        if (!checkArgumentTypes(expectedTypes)) {
            expression.type = MetaType.Error
            return
        }

        // verify the param type was defined
        if (paramReturnType == null) {
            expression.reportError(diagnostics, "Param return type was not able to be found.")
            expression.type = MetaType.Error
            return
        }

        expression.type = paramReturnType
    }

    private companion object {
        val IF_BASE_EXPECTED_TYPES = TupleType(
            ParamCommandHandler.PARAM_ANY,
            ScriptVarType.COMPONENT,
            PrimitiveType.INT,
        )
        val CC_BASE_EXPECTED_TYPES = TupleType(ParamCommandHandler.PARAM_ANY)
    }
}
