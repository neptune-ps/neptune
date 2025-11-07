package me.filby.neptune.clientscript.compiler.command

import me.filby.neptune.clientscript.compiler.type.ParamType
import me.filby.neptune.runescript.compiler.codegen.Opcode
import me.filby.neptune.runescript.compiler.configuration.command.CodeGeneratorContext
import me.filby.neptune.runescript.compiler.configuration.command.DynamicCommandHandler
import me.filby.neptune.runescript.compiler.configuration.command.TypeCheckingContext
import me.filby.neptune.runescript.compiler.type
import me.filby.neptune.runescript.compiler.type.MetaType
import me.filby.neptune.runescript.compiler.type.PrimitiveType
import me.filby.neptune.runescript.compiler.type.TupleType

class IfChildrenFilterCommandHandler : DynamicCommandHandler {
    override fun TypeCheckingContext.typeCheck() {
        // check param reference
        val paramExpr = checkArgument(0, ParamCommandHandler.PARAM_ANY)
        val paramReturnType = (paramExpr?.type as? ParamType)?.inner
        checkArgument(1, paramReturnType)

        val expectedTypes = TupleType(
            ParamCommandHandler.PARAM_ANY,
            paramReturnType ?: MetaType.Any,
        )
        checkArgumentTypes(expectedTypes)
        expression.type = PrimitiveType.INT
    }

    override fun CodeGeneratorContext.generateCode() {
        val valueArgument = expression.arguments[1]
        val valueType = checkNotNull(valueArgument.type.baseType)

        expression.arguments.visit()
        instruction(Opcode.PushConstantInt, valueType.id)
        command()
    }
}
