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

class CcFindParamCommandHandler : DynamicCommandHandler {
    override fun TypeCheckingContext.typeCheck() {
        checkArgument(0, ScriptVarType.COMPONENT)
        val param1Expr = checkArgument(1, ParamCommandHandler.PARAM_ANY)
        val param1ReturnType = (param1Expr?.type as? ParamType)?.inner
        checkArgument(2, param1ReturnType)

        val param2Expr = checkArgument(3, ParamCommandHandler.PARAM_ANY)
        val param2ReturnType = (param2Expr?.type as? ParamType)?.inner
        if (param2ReturnType != null) {
            checkArgument(4, param2ReturnType)
        }

        val expectedTypes = if (expression.arguments.size == 5) {
            // expect (component, param<T>, T, param<U>, U)
            TupleType(
                ScriptVarType.COMPONENT,
                ParamCommandHandler.PARAM_ANY,
                param1ReturnType ?: MetaType.Any,
                ParamCommandHandler.PARAM_ANY,
                param2ReturnType ?: MetaType.Any,
            )
        } else {
            // expect (component, param<T>, T)
            TupleType(
                ScriptVarType.COMPONENT,
                ParamCommandHandler.PARAM_ANY,
                param1ReturnType ?: MetaType.Any,
            )
        }

        checkArgumentTypes(expectedTypes)
        expression.type = PrimitiveType.BOOLEAN
    }

    override fun CodeGeneratorContext.generateCode() {
        expression.arguments.visit()

        val param1Type = (expression.arguments[1].type as ParamType).inner
        val param1BaseType = checkNotNull(param1Type.baseType)
        if (expression.arguments.size == 3) {
            // in the case of only having 3 arguments, the value for param2 is -1 and the basevartype id is also -1.
            // value2 is absent in this case.
            instruction(Opcode.PushConstantInt, -1)
            instruction(Opcode.PushConstantInt, param1BaseType.id)
            instruction(Opcode.PushConstantInt, -1)
        } else {
            val param2Type = (expression.arguments[3].type as ParamType).inner
            val param2BaseType = checkNotNull(param2Type.baseType)
            instruction(Opcode.PushConstantInt, param1BaseType.id)
            instruction(Opcode.PushConstantInt, param2BaseType.id)
        }

        command()
    }
}
