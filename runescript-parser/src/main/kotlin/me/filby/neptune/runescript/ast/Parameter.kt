package me.filby.neptune.runescript.ast

import com.google.common.base.MoreObjects
import com.google.common.base.Objects
import me.filby.neptune.runescript.ast.expr.Identifier
import me.filby.neptune.runescript.type.Type

/**
 * Represents a single parameter in a [Script].
 *
 * Example:
 * ```
 * int $some_name
 * ```
 */
public class Parameter(
    public val type: Type,
    public val name: Identifier,
    public val isArray: Boolean = false
) : Node() {

    init {
        addChild(name)
    }

    override fun <R> accept(visitor: AstVisitor<R>): R {
        return visitor.visitParameter(this)
    }

    override fun hashCode(): Int {
        return Objects.hashCode(type, name, isArray)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other !is Parameter) {
            return false
        }

        return type == other.type
            && name == other.name
            && isArray == other.isArray
    }

    override fun toString(): String {
        return MoreObjects.toStringHelper(this)
            .add("type", type)
            .add("name", name)
            .add("isArray", isArray)
            .toString()
    }

}
