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
 * the `array_min` and `array_max` commands within scripts.
 *
 * Example:
 * ```
 * def_int $min = array_min($intarray);
 * def_int $max = array_max($intarray);
 * ```
 */
class ArrayMinMaxCommandHandler : DynamicCommandHandler {
    override fun TypeCheckingContext.typeCheck() {
        val array1Expr = checkArgument(0, ArrayType(MetaType.Any))
        val array1ExprType = array1Expr?.type

        // check the base signature matches
        if (!checkArgumentTypes(BASE_EXPECTED_TYPES) || array1ExprType !is ArrayType) {
            expression.type = MetaType.Error
            return
        }

        val validType = ALLOWED_TYPES.any { checkArgumentTypes(ArrayType(it), false) }
        if (!validType) {
            expression.reportError(diagnostics, INVALID_TYPE_MESSAGE, array1ExprType.representation)
            expression.type = MetaType.Error
            return
        }

        expression.type = array1ExprType.inner
    }

    private companion object {
        const val INVALID_TYPE_MESSAGE = "Type mismatch: '%s' was given but 'intarray' or 'stringarray' was expected."
        val BASE_EXPECTED_TYPES = TupleType(ArrayType(MetaType.Any))
        val ALLOWED_TYPES = listOf(PrimitiveType.INT, PrimitiveType.STRING)
    }
}
