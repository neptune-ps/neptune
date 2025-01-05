package me.filby.neptune.runescript.ast.statement

import com.google.common.base.MoreObjects
import me.filby.neptune.runescript.ast.AstVisitor
import me.filby.neptune.runescript.ast.NodeSourceLocation
import me.filby.neptune.runescript.ast.expr.Expression
import java.util.Objects

/**
 * Represents an if statement that has a [condition], [thenStatement], and an optional [elseStatement].
 *
 * Example:
 * ```
 * if ($var1 = $var2) {
 *     mes("equal");
 * } else {
 *     mes("not equal");
 * }
 * ```
 */
public class IfStatement(
    source: NodeSourceLocation,
    public val condition: Expression,
    public val thenStatement: Statement,
    public val elseStatement: Statement?,
) : Statement(source) {
    init {
        addChild(condition)
        addChild(thenStatement)
        addChild(elseStatement)
    }

    override fun <R> accept(visitor: AstVisitor<R>): R = visitor.visitIfStatement(this)

    override fun hashCode(): Int = Objects.hash(condition, thenStatement, elseStatement)

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other !is IfStatement) {
            return false
        }

        return condition == other.condition &&
            thenStatement == other.thenStatement &&
            elseStatement == other.elseStatement
    }

    override fun toString(): String = MoreObjects.toStringHelper(this)
        .add("condition", condition)
        .add("thenStatement", thenStatement)
        .add("elseStatement", elseStatement)
        .toString()
}
