package me.filby.neptune.runescript.ast.expr

import com.google.common.base.MoreObjects
import me.filby.neptune.runescript.ast.AstVisitor
import java.util.*

/**
 * The base expression for all types of call expressions.
 */
public sealed class CallExpression(
    public val name: Identifier,
    public val arguments: List<Expression>
) : Expression() {

    init {
        addChild(name)
        addChild(arguments)
    }

    override fun toString(): String {
        return MoreObjects.toStringHelper(this)
            .add("name", name)
            .add("arguments", arguments)
            .toString()
    }

}

/**
 * A [CallExpression] for command calls.
 *
 * Example:
 * ```
 * cc_settext("Example text")
 * ```
 */
public class CommandCallExpression(name: Identifier, arguments: List<Expression>) : CallExpression(name, arguments) {

    override fun <R> accept(visitor: AstVisitor<R>): R {
        return visitor.visitCommandCallExpression(this)
    }

    override fun hashCode(): Int {
        return Objects.hash(name, arguments)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other !is CommandCallExpression) {
            return false
        }

        return name == other.name
            && arguments == other.arguments
    }

}

/**
 * A [CallExpression] for calling other (proc) scripts.
 *
 * ```
 * ~some_user_defined_script(true);
 * ```
 */
public class ProcCallExpression(name: Identifier, arguments: List<Expression>) : CallExpression(name, arguments) {

    override fun <R> accept(visitor: AstVisitor<R>): R {
        return visitor.visitProcCallExpression(this)
    }

    override fun hashCode(): Int {
        return Objects.hash(name, arguments)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other !is ProcCallExpression) {
            return false
        }

        return name == other.name
            && arguments == other.arguments
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
public class JumpCallExpression(name: Identifier, arguments: List<Expression>) : CallExpression(name, arguments) {

    override fun <R> accept(visitor: AstVisitor<R>): R {
        return visitor.visitJumpCallExpression(this)
    }

    override fun hashCode(): Int {
        return Objects.hash(name, arguments)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other !is JumpCallExpression) {
            return false
        }

        return name == other.name
            && arguments == other.arguments
    }

}
