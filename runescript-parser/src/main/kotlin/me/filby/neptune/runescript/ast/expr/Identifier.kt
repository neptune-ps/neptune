package me.filby.neptune.runescript.ast.expr

import com.google.common.base.MoreObjects
import me.filby.neptune.runescript.ast.AstVisitor
import me.filby.neptune.runescript.ast.NodeSourceLocation
import java.util.Objects

/**
 * Represents some kind of identifier within code.
 *
 * Examples: `abyssal_whip`, `smithing:arrowheads`.
 */
public class Identifier(source: NodeSourceLocation, public val text: String) : Expression(source) {
    override fun <R> accept(visitor: AstVisitor<R>): R = visitor.visitIdentifier(this)

    override fun hashCode(): Int = Objects.hash(text)

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other !is Identifier) {
            return false
        }

        return text == other.text
    }

    override fun toString(): String = MoreObjects.toStringHelper(this)
        .add("text", text)
        .toString()
}
