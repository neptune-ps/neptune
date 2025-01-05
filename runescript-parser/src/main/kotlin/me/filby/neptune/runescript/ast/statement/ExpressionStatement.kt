package me.filby.neptune.runescript.ast.statement

import com.google.common.base.MoreObjects
import me.filby.neptune.runescript.ast.AstVisitor
import me.filby.neptune.runescript.ast.NodeSourceLocation
import me.filby.neptune.runescript.ast.expr.Expression
import java.util.Objects

/**
 * Represents an [Expression] that is being called as a statement.
 *
 * Example:
 * ```
 * <cc_settext("Example text")>;
 * ```
 */
public class ExpressionStatement(source: NodeSourceLocation, public val expression: Expression) : Statement(source) {
    init {
        addChild(expression)
    }

    override fun <R> accept(visitor: AstVisitor<R>): R = visitor.visitExpressionStatement(this)

    override fun hashCode(): Int = Objects.hashCode(expression)

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other !is ExpressionStatement) {
            return false
        }

        return expression == other.expression
    }

    override fun toString(): String = MoreObjects.toStringHelper(this)
        .add("expression", expression)
        .toString()
}
