package me.filby.neptune.runescript.ast.statement

import com.google.common.base.MoreObjects
import me.filby.neptune.runescript.ast.AstVisitor
import me.filby.neptune.runescript.ast.expr.Expression
import java.util.*

/**
 * Represents a while statement with a [condition] and the [thenStatement] that is ran when the condition is `true`.
 *
 * Example:
 * ```
 * while ($var < 10) {
 *     mes(tostring($var));
 *     $var = calc($var + 1);
 * }
 * ```
 */
public class WhileStatement(
    public val condition: Expression,
    public val thenStatement: Statement,
) : Statement() {

    override fun <R> accept(visitor: AstVisitor<R>): R {
        return visitor.visitWhileStatement(this)
    }

    override fun hashCode(): Int {
        return Objects.hash(condition, thenStatement)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other !is WhileStatement) {
            return false
        }

        return condition == other.condition
            && thenStatement == other.thenStatement
    }

    override fun toString(): String {
        return MoreObjects.toStringHelper(this)
            .add("condition", condition)
            .add("thenStatement", thenStatement)
            .toString()
    }

}
