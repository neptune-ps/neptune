package me.filby.neptune.runescript.ast.statement

import com.google.common.base.MoreObjects
import me.filby.neptune.runescript.ast.AstVisitor
import me.filby.neptune.runescript.ast.NodeSourceLocation
import java.util.Objects

/**
 * Represents a block of statements.
 *
 * Example:
 * ```
 * {
 *    <code here>
 * }
 * ```
 */
public class BlockStatement(source: NodeSourceLocation, public val statements: List<Statement>) : Statement(source) {
    init {
        addChild(statements)
    }

    override fun <R> accept(visitor: AstVisitor<R>): R {
        return visitor.visitBlockStatement(this)
    }

    override fun hashCode(): Int {
        return Objects.hashCode(statements)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other !is BlockStatement) {
            return false
        }

        return statements == other.statements
    }

    override fun toString(): String {
        return MoreObjects.toStringHelper(this)
            .add("statements", statements)
            .toString()
    }
}
