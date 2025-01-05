package me.filby.neptune.runescript.ast

import com.google.common.base.MoreObjects
import com.google.common.base.Objects
import me.filby.neptune.runescript.ast.expr.Identifier

/**
 * Represents a single parameter in a [Script].
 *
 * Example:
 * ```
 * int $some_name
 * ```
 */
public class Parameter(source: NodeSourceLocation, public val typeToken: Token, public val name: Identifier) :
    Node(source) {
    init {
        addChild(typeToken)
        addChild(name)
    }

    override fun <R> accept(visitor: AstVisitor<R>): R = visitor.visitParameter(this)

    override fun hashCode(): Int = Objects.hashCode(typeToken, name)

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other !is Parameter) {
            return false
        }

        return typeToken == other.typeToken && name == other.name
    }

    override fun toString(): String = MoreObjects.toStringHelper(this)
        .add("typeToken", typeToken)
        .add("name", name)
        .toString()
}
