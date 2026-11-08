package me.filby.neptune.clientscript.compiler.command.array

import me.filby.neptune.clientscript.compiler.type.ScriptVarType
import me.filby.neptune.runescript.compiler.configuration.command.DynamicCommandHandler
import me.filby.neptune.runescript.compiler.configuration.command.TypeCheckingContext
import me.filby.neptune.runescript.compiler.type
import me.filby.neptune.runescript.compiler.type.MetaType
import me.filby.neptune.runescript.compiler.type.Type
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
class ArrayMinMaxCommandHandler(longArraySupport: Boolean) : DynamicCommandHandler {
    private val allowedTypes = buildList {
        add(ArrayType(ScriptVarType.INT))
        add(ArrayType(ScriptVarType.STRING))
        if (longArraySupport) {
            add(ArrayType(ScriptVarType.LONG))
        }
    }

    override fun TypeCheckingContext.typeCheck() {
        val array1Expr = checkArgument(0, ArrayType(MetaType.Any))
        val array1ExprType = array1Expr?.type

        // check the base signature matches
        if (!checkArgumentTypes(BASE_EXPECTED_TYPES) || array1ExprType !is ArrayType) {
            expression.type = MetaType.Error
            return
        }

        val validType = allowedTypes.any { checkArgumentTypes(it, false) }
        if (!validType) {
            expression.reportError(
                diagnostics,
                INVALID_TYPE_MESSAGE,
                array1ExprType.representation,
                allowedTypes.joinWithOr(),
            )
            expression.type = MetaType.Error
            return
        }

        expression.type = array1ExprType.inner
    }

    private companion object {
        const val INVALID_TYPE_MESSAGE = "Type mismatch: '%s' was given but %s was expected."
        val BASE_EXPECTED_TYPES = ArrayType(MetaType.Any)

        fun List<Type>.joinWithOr(): String {
            val names = map { "'${it.representation}'" }
            return when (names.size) {
                0, 1 -> names.joinToString()
                2 -> names.joinToString(" or ")
                else -> "${names.dropLast(1).joinToString(", ")}, or ${names.last()}"
            }
        }
    }
}
