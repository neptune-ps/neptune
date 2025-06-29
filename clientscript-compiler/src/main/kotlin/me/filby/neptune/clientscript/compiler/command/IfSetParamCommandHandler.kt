package me.filby.neptune.clientscript.compiler.command

import me.filby.neptune.clientscript.compiler.type.ParamType
import me.filby.neptune.clientscript.compiler.type.ScriptVarType
import me.filby.neptune.runescript.compiler.codegen.Opcode
import me.filby.neptune.runescript.compiler.configuration.command.CodeGeneratorContext
import me.filby.neptune.runescript.compiler.configuration.command.DynamicCommandHandler
import me.filby.neptune.runescript.compiler.configuration.command.TypeCheckingContext
import me.filby.neptune.runescript.compiler.type
import me.filby.neptune.runescript.compiler.type.MetaType
import me.filby.neptune.runescript.compiler.type.PrimitiveType
import me.filby.neptune.runescript.compiler.type.TupleType
import me.filby.neptune.runescript.compiler.type.Type

/**
 * An implementation of [DynamicCommandHandler] that adds support for type checking
 * the `if_setparam` and `cc_setparam` commands within scripts.
 *
 * Example:
 * ```
 * if_setparam(some_int_param, 42, $comp, null);
 * cc_setparam(some_int_param, 42);
 * ```
 */
class IfSetParamCommandHandler(private val cc: Boolean) : DynamicCommandHandler {
    override fun TypeCheckingContext.typeCheck() {
        // check param reference
        val paramExpr = checkArgument(0, ParamCommandHandler.PARAM_ANY)
        val paramReturnType = (paramExpr?.type as? ParamType)?.inner
        checkArgument(1, paramReturnType)
        if (!cc) {
            checkArgument(2, ScriptVarType.COMPONENT)
            checkArgument(3, PrimitiveType.INT)
        }

        // define the expected types based on what is currently known
        val expectedBaseTypes = if (!cc) {
            IF_BASE_EXPECTED_TYPES
        } else {
            CC_BASE_EXPECTED_TYPES
        }

        // compare against the base type before we compare against the more concrete types
        if (checkArgumentTypes(expectedBaseTypes) && paramReturnType is Type) {
            // expect (param<T>, T, component, int) or (param<T>, T)
            val expectedTypes = if (!cc) {
                TupleType(
                    ParamCommandHandler.PARAM_ANY,
                    paramReturnType,
                    ScriptVarType.COMPONENT,
                    PrimitiveType.INT,
                )
            } else {
                TupleType(ParamCommandHandler.PARAM_ANY, paramReturnType)
            }
            checkArgumentTypes(expectedTypes)
        }

        // verify the param type was defined
        if (paramReturnType == null) {
            expression.reportError(diagnostics, "Param return type was not able to be found.")
        }

        expression.type = MetaType.Unit
    }

    override fun CodeGeneratorContext.generateCode() {
        val valueArgument = expression.arguments[1]
        val valueType = checkNotNull(valueArgument.type.baseType)

        expression.arguments.visit()
        instruction(Opcode.PushConstantInt, valueType.id)
        command()
    }

    private companion object {
        val IF_BASE_EXPECTED_TYPES = TupleType(
            ParamCommandHandler.PARAM_ANY,
            MetaType.Any,
            ScriptVarType.COMPONENT,
            PrimitiveType.INT,
        )
        val CC_BASE_EXPECTED_TYPES = TupleType(ParamCommandHandler.PARAM_ANY, MetaType.Any)
    }
}
