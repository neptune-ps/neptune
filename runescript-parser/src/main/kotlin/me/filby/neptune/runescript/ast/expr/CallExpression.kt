package me.filby.neptune.runescript.ast.expr

import com.google.common.base.MoreObjects
import me.filby.neptune.runescript.ast.AstVisitor
import java.util.*

public class CallExpression(
    public val name: Identifier,
    public val arguments: List<Expression>
) : Expression() {

    override fun <R> accept(visitor: AstVisitor<R>): R {
        TODO("Not yet implemented")
    }

    override fun hashCode(): Int {
        return Objects.hash(name, arguments)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other !is CallExpression) {
            return false
        }

        return name == other.name
            && arguments == other.arguments
    }

    override fun toString(): String {
        return MoreObjects.toStringHelper(this)
            .add("name", name)
            .add("arguments", arguments)
            .toString()
    }

}
