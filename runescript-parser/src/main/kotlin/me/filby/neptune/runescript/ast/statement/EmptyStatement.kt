package me.filby.neptune.runescript.ast.statement

import com.google.common.base.MoreObjects
import me.filby.neptune.runescript.ast.AstVisitor
import me.filby.neptune.runescript.ast.NodeSourceLocation

/**
 * Represents a statement with no code attached.
 *
 * Example:
 * ```
 * ;
 * ```
 */
public class EmptyStatement(source: NodeSourceLocation) : Statement(source) {
    override fun <R> accept(visitor: AstVisitor<R>): R = visitor.visitEmptyStatement(this)

    override fun hashCode(): Int = EmptyStatement::class.java.hashCode()

    override fun equals(other: Any?): Boolean {
        if (other is EmptyStatement) {
            return true
        }
        return false
    }

    override fun toString(): String = MoreObjects.toStringHelper(this)
        .toString()
}
