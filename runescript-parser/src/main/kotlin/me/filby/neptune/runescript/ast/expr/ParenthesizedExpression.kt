package me.filby.neptune.runescript.ast.expr

import com.google.common.base.MoreObjects
import me.filby.neptune.runescript.ast.AstVisitor
import java.util.*

/**
 * Represents an expression that was wrapped in parenthesis.
 *
 * Example:
 * ```
 * ($var1 = 0 | $var2 = 0) & $var3 = 1
 * ```
 */
public class ParenthesizedExpression(public val expression: Expression) : Expression() {

    override fun <R> accept(visitor: AstVisitor<R>): R {
        return visitor.visitParenthesizedExpression(this)
    }

    override fun hashCode(): Int {
        return Objects.hashCode(expression)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other !is ParenthesizedExpression) {
            return false
        }

        return expression == other.expression
    }

    override fun toString(): String {
        return MoreObjects.toStringHelper(this)
            .add("expression", expression)
            .toString()
    }

}
