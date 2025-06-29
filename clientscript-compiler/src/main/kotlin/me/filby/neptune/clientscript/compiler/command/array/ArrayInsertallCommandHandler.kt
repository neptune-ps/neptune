package me.filby.neptune.clientscript.compiler.command.array

import me.filby.neptune.runescript.compiler.configuration.command.DynamicCommandHandler
import me.filby.neptune.runescript.compiler.configuration.command.TypeCheckingContext
import me.filby.neptune.runescript.compiler.type
import me.filby.neptune.runescript.compiler.type.MetaType
import me.filby.neptune.runescript.compiler.type.PrimitiveType
import me.filby.neptune.runescript.compiler.type.TupleType
import me.filby.neptune.runescript.compiler.type.wrapped.ArrayType

/**
 * An implementation of [DynamicCommandHandler] that adds support for type checking
 * the `array_insertall` command within scripts.
 *
 * Example:
 * ```
 * array_insertall($array1, $array2, 0)
 * ```
 */
class ArrayInsertallCommandHandler : DynamicCommandHandler {
    override fun TypeCheckingContext.typeCheck() {
        val array1Expr = checkArgument(0, ArrayType(MetaType.Any))
        val array1ExprType = array1Expr?.type
        checkArgument(1, array1ExprType)
        checkArgument(2, PrimitiveType.INT)

        // check the base signature matches
        if (checkArgumentTypes(BASE_EXPECTED_TYPES) && array1ExprType is ArrayType) {
            val expectedTypes = TupleType(
                array1ExprType,
                array1ExprType,
                PrimitiveType.INT,
            )
            checkArgumentTypes(expectedTypes)
        }

        expression.type = MetaType.Unit
    }

    private companion object {
        val BASE_EXPECTED_TYPES = TupleType(
            ArrayType(MetaType.Any),
            ArrayType(MetaType.Any),
            PrimitiveType.INT,
        )
    }
}
