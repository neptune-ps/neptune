package me.filby.neptune.runescript.ast

import com.google.common.base.MoreObjects
import java.util.*

public class Identifier(public val text: String) : Node() {

    override fun <R> accept(visitor: AstVisitor<R>): R {
        return visitor.visitIdentifier(this)
    }

    override fun hashCode(): Int {
        return Objects.hash(text)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true;
        }

        if (other !is Identifier) {
            return false;
        }

        return Objects.equals(text, other.text)
    }

    override fun toString(): String {
        return MoreObjects.toStringHelper(this)
            .add("text", text)
            .toString()
    }

}
