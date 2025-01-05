package me.filby.neptune.runescript.ast.expr

import com.google.common.base.MoreObjects
import me.filby.neptune.runescript.ast.AstVisitor
import me.filby.neptune.runescript.ast.NodeSourceLocation
import java.util.Objects

/**
 * An expression that allows doing math operations inside.
 *
 * Example:
 * ```
 * calc(1 + 1 / 2)
 * ```
 */
public class CalcExpression(source: NodeSourceLocation, public val expression: Expression) : Expression(source) {
    init {
        addChild(expression)
    }

    override fun <R> accept(visitor: AstVisitor<R>): R = visitor.visitCalcExpression(this)

    override fun hashCode(): Int = Objects.hashCode(expression)

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other !is CalcExpression) {
            return false
        }

        return expression == other.expression
    }

    override fun toString(): String = MoreObjects.toStringHelper(this)
        .add("expression", expression)
        .toString()
}
