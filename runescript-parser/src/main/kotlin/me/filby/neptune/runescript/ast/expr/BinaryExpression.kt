package me.filby.neptune.runescript.ast.expr

import com.google.common.base.MoreObjects
import me.filby.neptune.runescript.ast.AstVisitor
import java.util.*

/**
 * An expression that has a [left] hand side and [right] hand side with an [operator] that specifies what to do
 * with both of them.
 *
 * Example:
 * ```
 * 1 + 1
 * ```
 */
public class BinaryExpression(
    public val left: Expression,
    public val operator: String,
    public val right: Expression
) : Expression() {

    override fun <R> accept(visitor: AstVisitor<R>): R {
        return visitor.visitBinaryExpression(this)
    }

    override fun hashCode(): Int {
        return Objects.hash(left, operator, right)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other !is BinaryExpression) {
            return false
        }

        return left == other.left
            && operator == other.operator
            && right == other.right
    }

    override fun toString(): String {
        return MoreObjects.toStringHelper(this)
            .add("left", left)
            .add("operator", operator)
            .add("right", right)
            .toString()
    }

}
