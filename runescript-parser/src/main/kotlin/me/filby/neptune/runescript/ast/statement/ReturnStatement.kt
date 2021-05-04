package me.filby.neptune.runescript.ast.statement

import com.google.common.base.MoreObjects
import me.filby.neptune.runescript.ast.AstVisitor
import me.filby.neptune.runescript.ast.expr.Expression
import java.util.*

public class ReturnStatement(public val expressions: List<Expression>) : Statement() {

    override fun <R> accept(visitor: AstVisitor<R>): R {
        return visitor.visitReturnStatement(this)
    }

    override fun hashCode(): Int {
        return Objects.hashCode(expressions)
    }

    override fun equals(other: Any?): Boolean {
        if (other === other) {
            return true
        }

        if (other !is ReturnStatement) {
            return false
        }

        return expressions == other.expressions
    }

    override fun toString(): String {
        return MoreObjects.toStringHelper(this)
            .add("expressions", expressions)
            .toString()
    }

}
