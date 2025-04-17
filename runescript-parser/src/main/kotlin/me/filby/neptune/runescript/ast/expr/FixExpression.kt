package me.filby.neptune.runescript.ast.expr

import com.google.common.base.MoreObjects
import me.filby.neptune.runescript.ast.AstVisitor
import me.filby.neptune.runescript.ast.NodeSourceLocation
import me.filby.neptune.runescript.ast.Token
import java.util.Objects

/**
 * A base interface for "Prefix" and "Postfix" operators.
 *
 * @see PrefixExpression
 * @see PostfixExpression
 */
public abstract class FixExpression(
    source: NodeSourceLocation,
    public val operator: Token,
    public val variable: Expression,
    public val isPrefix: Boolean,
) : Expression(source) {
    init {
        addChild(operator)
        addChild(variable)
    }

    override fun hashCode(): Int {
        return Objects.hash(operator, variable)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other !is PrefixExpression) {
            return false
        }

        return operator == other.operator &&
            variable == other.variable
    }

    override fun toString(): String {
        return MoreObjects.toStringHelper(this)
            .add("operator", operator)
            .add("variable", variable)
            .add("isPrefix", isPrefix)
            .toString()
    }
}

/**
 * A [FixExpression] implementation used for prefix expressions.
 *
 * Example:
 * ```runescript
 * ++$var
 * ```
 *
 * @see FixExpression
 */
public class PrefixExpression(
    source: NodeSourceLocation,
    operator: Token,
    variable: Expression,
) : FixExpression(source, operator, variable, false) {
    override fun <R> accept(visitor: AstVisitor<R>): R {
        return visitor.visitPrefixExpression(this)
    }
}

/**
 * A [FixExpression] implementation used for postfix expressions.
 *
 * Example:
 * ```runescript
 * $var++
 * ```
 *
 * @see FixExpression
 */
public class PostfixExpression(
    source: NodeSourceLocation,
    operator: Token,
    variable: Expression,
) : FixExpression(source, operator, variable, true) {

    override fun <R> accept(visitor: AstVisitor<R>): R {
        return visitor.visitPostfixExpression(this)
    }
}
