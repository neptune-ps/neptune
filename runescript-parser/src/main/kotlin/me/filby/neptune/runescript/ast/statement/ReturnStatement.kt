package me.filby.neptune.runescript.ast.statement

import com.google.common.base.MoreObjects
import me.filby.neptune.runescript.ast.AstVisitor
import me.filby.neptune.runescript.ast.NodeSourceLocation
import me.filby.neptune.runescript.ast.expr.Expression
import java.util.Objects

/**
 * Represents a return statement that can have any number of [expressions].
 *
 * Example:
 * ```
 * return(1, 2, 3);
 * ```
 */
public class ReturnStatement(source: NodeSourceLocation, public val expressions: List<Expression>) : Statement(source) {
    init {
        addChild(expressions)
    }

    override fun <R> accept(visitor: AstVisitor<R>): R = visitor.visitReturnStatement(this)

    override fun hashCode(): Int = Objects.hashCode(expressions)

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other !is ReturnStatement) {
            return false
        }

        return expressions == other.expressions
    }

    override fun toString(): String = MoreObjects.toStringHelper(this)
        .add("expressions", expressions)
        .toString()
}
