package me.filby.neptune.runescript.ast.statement

import com.google.common.base.MoreObjects
import me.filby.neptune.runescript.ast.AstVisitor
import me.filby.neptune.runescript.ast.NodeSourceLocation
import me.filby.neptune.runescript.ast.expr.Expression
import me.filby.neptune.runescript.ast.expr.VariableExpression
import java.util.Objects

/**
 * Represents a statement that defines [vars] (LHS) to set their values to the result of [expressions] (RHS).
 *
 * Syntax:
 * ```
 * $var1, $var2 = 1, 2;
 * ```
 */
public class AssignmentStatement(
    source: NodeSourceLocation,
    public val vars: List<VariableExpression>,
    public val expressions: List<Expression>,
) : Statement(source) {
    init {
        addChild(vars)
        addChild(expressions)
    }

    override fun <R> accept(visitor: AstVisitor<R>): R = visitor.visitAssignmentStatement(this)

    override fun hashCode(): Int = Objects.hash(vars, expressions)

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other !is AssignmentStatement) {
            return false
        }

        return vars == other.vars && expressions == other.expressions
    }

    override fun toString(): String = MoreObjects.toStringHelper(this)
        .add("vars", vars)
        .add("expressions", expressions)
        .toString()
}
