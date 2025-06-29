package me.filby.neptune.clientscript.compiler.command.array

import me.filby.neptune.runescript.compiler.codegen.Opcode
import me.filby.neptune.runescript.compiler.configuration.command.CodeGeneratorContext
import me.filby.neptune.runescript.compiler.configuration.command.DynamicCommandHandler
import me.filby.neptune.runescript.compiler.configuration.command.TypeCheckingContext
import me.filby.neptune.runescript.compiler.type
import me.filby.neptune.runescript.compiler.type.MetaType
import me.filby.neptune.runescript.compiler.type.PrimitiveType
import me.filby.neptune.runescript.compiler.type.TupleType
import me.filby.neptune.runescript.compiler.type.wrapped.ArrayType

/**
 * An implementation of [DynamicCommandHandler] that adds support for type checking
 * the `array_fill` command within scripts.
 *
 * Example:
 * ```
 * array_fill($intarray, 1, 0, 10)
 * ```
 */
class ArrayFillCommandHandler : DynamicCommandHandler {
    override fun TypeCheckingContext.typeCheck() {
        val arrayExpr = checkArgument(0, ArrayType(MetaType.Any))
        val arrayExprType = arrayExpr?.type
        val arrayExprInnerType = (arrayExprType as? ArrayType)?.inner
        checkArgument(1, arrayExprInnerType)
        checkArgument(2, PrimitiveType.INT)
        checkArgument(3, PrimitiveType.INT)

        // check the base signature matches
        if (checkArgumentTypes(BASE_EXPECTED_TYPES) && arrayExprType is ArrayType) {
            // expect (array<T>, T, int, int)
            val expectedTypes = TupleType(arrayExprType, arrayExprType.inner, PrimitiveType.INT, PrimitiveType.INT)
            checkArgumentTypes(expectedTypes)
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
        val BASE_EXPECTED_TYPES = TupleType(
            ArrayType(MetaType.Any),
            MetaType.Any,
            PrimitiveType.INT,
            PrimitiveType.INT,
        )
    }
}
