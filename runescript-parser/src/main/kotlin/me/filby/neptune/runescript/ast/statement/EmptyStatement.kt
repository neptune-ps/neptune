package me.filby.neptune.runescript.ast.statement

import com.google.common.base.MoreObjects
import me.filby.neptune.runescript.ast.AstVisitor

/**
 * Represents a statement with no code attached.
 *
 * Example:
 * ```
 * ;
 * ```
 */
public object EmptyStatement : Statement() {

    override fun <R> accept(visitor: AstVisitor<R>): R {
        return visitor.visitEmptyStatement(this)
    }

    override fun hashCode(): Int {
        return EmptyStatement::class.java.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return this === other
    }

    override fun toString(): String {
        return MoreObjects.toStringHelper(this)
            .toString()
    }

}
