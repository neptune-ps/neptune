package me.filby.neptune.runescript.ast

import com.google.common.base.MoreObjects
import com.google.common.base.Objects

/**
 * A simple node that contains an antlr [org.antlr.v4.runtime.Token] text.
 */
public class Token(source: NodeSourceLocation, public val text: String) : Node(source) {
    override fun <R> accept(visitor: AstVisitor<R>): R {
        // TODO should we actually implement this?
        error("Token#accept should never be called.")
    }

    override fun hashCode(): Int {
        return Objects.hashCode(text)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other !is Token) {
            return false
        }

        return text == other.text
    }

    override fun toString(): String {
        return MoreObjects.toStringHelper(this)
            .add("text", text)
            .toString()
    }
}
