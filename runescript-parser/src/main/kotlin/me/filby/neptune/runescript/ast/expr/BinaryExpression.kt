package me.filby.neptune.runescript.ast.expr

import com.google.common.base.MoreObjects
import me.filby.neptune.runescript.ast.AstVisitor
import me.filby.neptune.runescript.ast.NodeSourceLocation
import me.filby.neptune.runescript.ast.Token
import java.util.Objects

/**
 * An expression that has a [left] hand side and [right] hand side with an [operator] that specifies what to do
 * with both of them.
 *
 * Example:
 * ```
 * 1 + 1
 * ```
 */
public open class BinaryExpression(
    source: NodeSourceLocation,
    public val left: Expression,
    public val operator: Token,
    public val right: Expression,
) : Expression(source) {
    init {
        addChild(left)
        addChild(operator)
        addChild(right)
    }

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

        return left == other.left &&
            operator == other.operator &&
            right == other.right
    }

    override fun toString(): String {
        return MoreObjects.toStringHelper(this)
            .add("left", left)
            .add("operator", operator)
            .add("right", right)
            .toString()
    }
}

/**
 * A type of [BinaryExpression] that is used for arithmetic within [CalcExpression].
 *
 * The valid operators are: `*` (multiply), `/` (divide), `%` (modulo), `+` (add), `-` (subtract), `&` (and), `|` (or).
 */
public class ArithmeticExpression(
    source: NodeSourceLocation,
    left: Expression,
    operator: Token,
    right: Expression,
) : BinaryExpression(source, left, operator, right) {
    override fun <R> accept(visitor: AstVisitor<R>): R {
        return visitor.visitArithmeticExpression(this)
    }
}
