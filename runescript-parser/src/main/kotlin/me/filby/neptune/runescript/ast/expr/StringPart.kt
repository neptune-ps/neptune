package me.filby.neptune.runescript.ast.expr

import com.google.common.base.MoreObjects
import me.filby.neptune.runescript.ast.AstVisitor
import me.filby.neptune.runescript.ast.Node
import me.filby.neptune.runescript.ast.NodeSourceLocation
import java.util.Objects

/**
 * Represents a piece of a [JoinedStringExpression].
 */
public sealed class StringPart(source: NodeSourceLocation) : Node(source) {
    override fun <R> accept(visitor: AstVisitor<R>): R = visitor.visitJoinedStringPart(this)
}

/**
 * A basic part that contains only text.
 */
public open class BasicStringPart(source: NodeSourceLocation, public val value: String) : StringPart(source) {
    override fun hashCode(): Int = Objects.hashCode(value)

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other !is BasicStringPart) {
            return false
        }

        return value == other.value
    }

    override fun toString(): String = MoreObjects.toStringHelper(this)
        .add("text", value)
        .toString()
}

/**
 * A basic part that contains a `<p,name>` tag.
 */
public class PTagStringPart(source: NodeSourceLocation, text: String) : BasicStringPart(source, text)

/**
 * A part that contains an [Expression] that will be executed.
 */
public class ExpressionStringPart(source: NodeSourceLocation, public val expression: Expression) : StringPart(source) {
    init {
        addChild(expression)
    }

    override fun hashCode(): Int = Objects.hashCode(expression)

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other !is ExpressionStringPart) {
            return false
        }

        return expression == other.expression
    }

    override fun toString(): String = MoreObjects.toStringHelper(this)
        .add("expression", expression)
        .toString()
}
