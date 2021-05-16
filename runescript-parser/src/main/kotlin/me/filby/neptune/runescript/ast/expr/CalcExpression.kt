package me.filby.neptune.runescript.ast.expr

import com.google.common.base.MoreObjects
import me.filby.neptune.runescript.ast.AstVisitor
import java.util.*

/**
 * An expression that allows doing math operations inside.
 *
 * Example:
 * ```
 * calc(1 + 1 / 2)
 * ```
 */
public class CalcExpression(public val expression: Expression) : Expression() {

    init {
        addChild(expression)
    }

    override fun <R> accept(visitor: AstVisitor<R>): R {
        return visitor.visitCalcExpression(this)
    }

    override fun hashCode(): Int {
        return Objects.hashCode(expression)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other !is CalcExpression) {
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
