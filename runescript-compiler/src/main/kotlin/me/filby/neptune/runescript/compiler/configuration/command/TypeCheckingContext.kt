package me.filby.neptune.runescript.compiler.configuration.command

import me.filby.neptune.runescript.ast.Node
import me.filby.neptune.runescript.ast.expr.CommandCallExpression
import me.filby.neptune.runescript.ast.expr.ConstantVariableExpression
import me.filby.neptune.runescript.ast.expr.Expression
import me.filby.neptune.runescript.ast.expr.Identifier
import me.filby.neptune.runescript.ast.expr.Literal
import me.filby.neptune.runescript.compiler.diagnostics.Diagnostic
import me.filby.neptune.runescript.compiler.diagnostics.DiagnosticMessage
import me.filby.neptune.runescript.compiler.diagnostics.DiagnosticType
import me.filby.neptune.runescript.compiler.diagnostics.Diagnostics
import me.filby.neptune.runescript.compiler.nullableType
import me.filby.neptune.runescript.compiler.reference
import me.filby.neptune.runescript.compiler.semantics.TypeChecking
import me.filby.neptune.runescript.compiler.symbol.BasicSymbol
import me.filby.neptune.runescript.compiler.symbol.ConfigSymbol
import me.filby.neptune.runescript.compiler.type
import me.filby.neptune.runescript.compiler.type.MetaType
import me.filby.neptune.runescript.compiler.type.TupleType
import me.filby.neptune.runescript.compiler.type.Type
import me.filby.neptune.runescript.compiler.type.TypeManager
import me.filby.neptune.runescript.compiler.typeHint

/**
 * Contains the context of the [TypeChecking] and supplies useful functions when
 * implementing a [DynamicCommandHandler].
 */
public data class TypeCheckingContext(
    private val typeChecker: TypeChecking,
    val typeManager: TypeManager,
    val expression: CommandCallExpression,
    val diagnostics: Diagnostics
) {
    /**
     * Whether the expression is a constant expression.
     *
     * A constant expression is defined as being one of the following:
     *  - [ConstantVariableExpression]
     *  - [Literal]
     *  - [Identifier] (see note below)
     *
     * Note: Identifiers that reference symbols other than [BasicSymbol] and [ConfigSymbol]
     * do not quality as a constant expression.
     */
    public val Expression?.isConstant: Boolean
        get() {
            if (this == null) {
                return false
            }
            return typeChecker.isConstantExpression(this)
        }

    /**
     * Checks the argument at [index]. If the argument exists then the `typeHint` of the
     * expression is set to [typeHint] and the argument is then passed through the visitor like
     * normal. Accessing `type` after this is safe as long as returned value is not `null`. The
     * returned value will only be `null` if the argument requested is out of bounds.
     *
     * Example:
     * ```
     * // check the argument with a type hint of obj
     * checkArgument(0, typeHint = PrimitiveType.OBJ)
     *
     * // verify the types match, if mismatch let the function report the error
     * checkParameterTypes(expected = PrimitiveType.OBJ)
     * ```
     *
     * @see checkTypeArgument
     */
    public fun checkArgument(index: Int, typeHint: Type?): Expression? {
        if (index !in expression.arguments.indices) {
            return null
        }
        val argument = expression.arguments[index]
        argument.visit(typeHint)
        return argument
    }

    /**
     * Checks the argument at [index]. If the argument exists is it validated to be a basic
     * [Identifier] and attempts to look up a type based on the identifier text. If the
     * argument does not exist (out of bounds), `null` is returned.
     *
     * If the expression is not an identifier or the type does not exist an error is logged.
     * The expressions type is assigned to either [MetaType.Error] in the case of error,
     * and [MetaType.Type] if successful.
     *
     * This should only be used when attempting to evaluate an argument as a type reference.
     *
     * @see checkArgument
     */
    public fun checkTypeArgument(index: Int): Expression? {
        if (index !in expression.arguments.indices) {
            return null
        }

        val argument = expression.arguments[index]

        // verify the argument is a constant expression
        if (argument !is Identifier) {
            diagnostics.report(Diagnostic(DiagnosticType.ERROR, argument, DIAGNOSTIC_TYPEREF_EXPECTED))
            argument.type = MetaType.Error
            return argument
        }

        // lookup the type by name
        val type = typeManager.find(argument.text)
        if (type == MetaType.Error) {
            // type doesn't exist so report an error
            diagnostics.report(
                Diagnostic(
                    DiagnosticType.ERROR,
                    argument,
                    DiagnosticMessage.GENERIC_INVALID_TYPE,
                    argument.text
                )
            )
            argument.type = MetaType.Error
            return argument
        }

        // assign the type and create a basic symbol with the type name and wrapped type
        argument.type = MetaType.Type(type)
        argument.reference = BasicSymbol(argument.text, argument.type)
        return argument
    }

    /**
     * Verifies that the command argument types matche [expected]. This function
     * should be used  for validation the argument types passed to the command.
     * When needing to compare multiple types, use [TupleType].
     *
     * If [reportError] is enabled and there is a type mismatch, an error is submitted to the
     * [diagnostics]. If this option is disabled, you _must_ use the return value to report
     * an error manually otherwise compilation will continue even though there was an error.
     *
     * The following example is type hinting to `int` and visiting the first argument if it exists.
     * Then it if verifying that the arguments passed to the command actually matches a single `int`.
     * ```
     * // check the argument while type hinting it as an int
     * checkArgument(0, typeHint = PrimitiveType.INT)
     *
     * // actually verify the arguments match to a single int
     * checkArgumentTypes(expected = PrimitiveType.INT)
     * ```
     */
    public fun checkArgumentTypes(expected: Type, reportError: Boolean = true): Boolean {
        // collect the argument types while visiting any arguments that have no type defined
        val argumentTypes = mutableListOf<Type>()
        for (arg in expression.arguments) {
            if (arg.nullableType == null) {
                arg.visit()
            }
            argumentTypes += arg.type
        }

        val actual = TupleType.fromList(argumentTypes)
        return typeChecker.checkTypeMatch(expression, expected, actual, reportError)
    }

    /**
     * Collects all [Expression.type] for the [expressions].
     *
     * @see TupleType.fromList
     */
    public fun collectTypes(vararg expressions: Expression?): Type {
        return TupleType.fromList(expressions.mapNotNull { it?.nullableType })
    }

    /**
     * Passes the node through the type checker if it is not `null`.
     */
    public fun Node?.visit() {
        typeChecker.run {
            visit()
        }
    }

    /**
     * Passes the expression through the type check if it is not `null`.
     */
    public fun Expression?.visit(typeHint: Type?) {
        typeChecker.run {
            if (typeHint != null) {
                this@visit?.typeHint = typeHint
            }
            visit()
        }
    }

    /**
     * Passes all nodes within the list through the type check if it is not `null`.
     */
    public fun List<Node>?.visit() {
        typeChecker.run {
            visit()
        }
    }

    private companion object {
        const val DIAGNOSTIC_TYPEREF_EXPECTED = "Type reference expected."
    }
}
