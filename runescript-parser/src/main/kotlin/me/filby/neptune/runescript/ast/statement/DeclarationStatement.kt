package me.filby.neptune.runescript.ast.statement

import com.google.common.base.MoreObjects
import me.filby.neptune.runescript.ast.AstVisitor
import me.filby.neptune.runescript.ast.NodeSourceLocation
import me.filby.neptune.runescript.ast.Token
import me.filby.neptune.runescript.ast.expr.Expression
import me.filby.neptune.runescript.ast.expr.Identifier
import java.util.Objects

/**
 * Represents a local variable declaration statement that defines the variables [typeToken], [name], and an optional
 * [initializer].
 *
 * Example:
 * ```
 * def_int $var1 = 0;
 * ```
 */
public class DeclarationStatement(
    source: NodeSourceLocation,
    public val typeToken: Token,
    public val name: Identifier,
    public val initializer: Expression?,
) : Statement(source) {
    init {
        addChild(typeToken)
        addChild(name)
        addChild(initializer)
    }

    override fun <R> accept(visitor: AstVisitor<R>): R = visitor.visitDeclarationStatement(this)

    override fun hashCode(): Int = Objects.hash(typeToken, name, initializer)

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other !is DeclarationStatement) {
            return false
        }

        return typeToken == other.typeToken && name == other.name && initializer == other.initializer
    }

    override fun toString(): String = MoreObjects.toStringHelper(this)
        .add("typeToken", typeToken)
        .add("name", name)
        .add("initializer", initializer)
        .toString()
}
