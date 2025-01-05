package me.filby.neptune.runescript.ast.expr

import com.google.common.base.MoreObjects
import me.filby.neptune.runescript.ast.AstVisitor
import me.filby.neptune.runescript.ast.NodeSourceLocation
import java.util.Objects

/**
 * The base expression for all types of call expressions.
 */
public sealed class CallExpression(
    source: NodeSourceLocation,
    public val name: Identifier,
    public val arguments: List<Expression>,
) : Expression(source) {
    init {
        addChild(name)
        addChild(arguments)
    }

    override fun toString(): String = MoreObjects.toStringHelper(this)
        .add("name", name)
        .add("arguments", arguments)
        .toString()
}

/**
 * A [CallExpression] for command calls.
 *
 * Example:
 * ```
 * cc_settext("Example text")
 * ```
 */
public class CommandCallExpression(source: NodeSourceLocation, name: Identifier, arguments: List<Expression>) :
    CallExpression(source, name, arguments) {
    override fun <R> accept(visitor: AstVisitor<R>): R = visitor.visitCommandCallExpression(this)

    override fun hashCode(): Int = Objects.hash(name, arguments)

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other !is CommandCallExpression) {
            return false
        }

        return name == other.name && arguments == other.arguments
    }
}

/**
 * A [CallExpression] for calling other (proc) scripts.
 *
 * ```
 * ~some_user_defined_script(true);
 * ```
 */
public class ProcCallExpression(source: NodeSourceLocation, name: Identifier, arguments: List<Expression>) :
    CallExpression(source, name, arguments) {
    override fun <R> accept(visitor: AstVisitor<R>): R = visitor.visitProcCallExpression(this)

    override fun hashCode(): Int = Objects.hash(name, arguments)

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other !is ProcCallExpression) {
            return false
        }

        return name == other.name && arguments == other.arguments
    }
}

/**
 * A [CallExpression] for jumping to a label.
 *
 * Example:
 * ```
 * @some_label(42);
 * ```
 */
public class JumpCallExpression(source: NodeSourceLocation, name: Identifier, arguments: List<Expression>) :
    CallExpression(source, name, arguments) {
    override fun <R> accept(visitor: AstVisitor<R>): R = visitor.visitJumpCallExpression(this)

    override fun hashCode(): Int = Objects.hash(name, arguments)

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other !is JumpCallExpression) {
            return false
        }

        return name == other.name && arguments == other.arguments
    }
}
