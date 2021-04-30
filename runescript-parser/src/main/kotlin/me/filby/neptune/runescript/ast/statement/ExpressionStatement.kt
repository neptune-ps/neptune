package me.filby.neptune.runescript.ast.statement

import com.google.common.base.MoreObjects
import me.filby.neptune.runescript.ast.AstVisitor
import me.filby.neptune.runescript.ast.expr.Expression
import java.util.*

public class ExpressionStatement(public val expression: Expression) : Statement() {

    override fun <R> accept(visitor: AstVisitor<R>): R {
        return visitor.visitExpressionStatement(this)
    }

    override fun hashCode(): Int {
        return Objects.hashCode(expression)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other !is ExpressionStatement) {
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
