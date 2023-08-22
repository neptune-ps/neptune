package me.filby.neptune.runescript.ast.expr

import com.google.common.base.MoreObjects
import me.filby.neptune.runescript.ast.AstVisitor
import me.filby.neptune.runescript.ast.NodeSourceLocation
import java.util.Objects

/**
 * A parsed ClientScript reference.
 *
 * Example:
 * ```
 * some_handler(){var1}
 * ```
 */
public class ClientScriptExpression(
    source: NodeSourceLocation,
    name: Identifier,
    arguments: List<Expression>,
    public val transmitList: List<Expression>
) : CallExpression(source, name, arguments) {
    init {
        addChild(transmitList)
    }

    override fun <R> accept(visitor: AstVisitor<R>): R {
        return visitor.visitClientScriptExpression(this)
    }

    override fun hashCode(): Int {
        return Objects.hash(name, arguments, transmitList)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other !is ClientScriptExpression) {
            return false
        }

        return name == other.name && arguments == other.arguments && transmitList == other.transmitList
    }

    override fun toString(): String {
        return MoreObjects.toStringHelper(this)
            .add("name", name)
            .add("arguments", arguments)
            .add("triggers", transmitList)
            .toString()
    }
}
