package me.filby.neptune.runescript.ast.statement

import com.google.common.base.MoreObjects
import me.filby.neptune.runescript.ast.AstVisitor
import me.filby.neptune.runescript.ast.expr.Expression
import me.filby.neptune.runescript.ast.expr.Identifier
import me.filby.neptune.runescript.type.Type
import java.util.Objects

/**
 * Represents a local variable declaration statement that defines the variables [type], [name], and an optional
 * [initializer].
 *
 * Example:
 * ```
 * def_int $var1 = 0;
 * ```
 */
public class DeclarationStatement(
    public val type: Type,
    public val name: Identifier,
    public val initializer: Expression?
) : Statement() {
    init {
        addChild(name)
        addChild(initializer)
    }

    override fun <R> accept(visitor: AstVisitor<R>): R {
        return visitor.visitDeclarationStatement(this)
    }

    override fun hashCode(): Int {
        return Objects.hash(type, name, initializer)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other !is DeclarationStatement) {
            return false
        }

        return type == other.type && name == other.name && initializer == other.initializer
    }

    override fun toString(): String {
        return MoreObjects.toStringHelper(this)
            .add("type", type)
            .add("name", name)
            .add("initializer", initializer)
            .toString()
    }
}
