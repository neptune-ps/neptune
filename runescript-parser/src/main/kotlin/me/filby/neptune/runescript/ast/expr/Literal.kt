package me.filby.neptune.runescript.ast.expr

import com.google.common.base.MoreObjects
import me.filby.neptune.runescript.ast.AstVisitor
import me.filby.neptune.runescript.ast.Node
import java.util.*

/**
 * An [Expression] that represents a constant value of [T].
 */
public sealed class Literal<T>(public val value: T) : Expression() {

    override fun hashCode(): Int {
        return Objects.hashCode(value)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other !is Literal<*>) {
            return false
        }

        return value == other.value
    }

    override fun toString(): String {
        return MoreObjects.toStringHelper(this)
            .add("value", value)
            .toString()
    }

}

/**
 * An implementation of [Literal] for numeric literals.
 *
 * Example:
 * ```
 * 123456
 * ```
 */
public class IntegerLiteral(value: Int) : Literal<Int>(value) {

    override fun <R> accept(visitor: AstVisitor<R>): R {
        return visitor.visitIntegerLiteral(this)
    }

}

/**
 * An implementation of [Literal] for boolean (`true`/`false`) literals.
 *
 * Example:
 * ```
 * true
 * ```
 */
public class BooleanLiteral(value: Boolean) : Literal<Boolean>(value) {

    override fun <R> accept(visitor: AstVisitor<R>): R {
        return visitor.visitBooleanLiteral(this)
    }

}

/**
 * An implementation of [Literal] for character literals.
 *
 * Example:
 * ```
 * 'c'
 * ```
 */
public class CharacterLiteral(value: Char) : Literal<Char>(value) {

    override fun <R> accept(visitor: AstVisitor<R>): R {
        return visitor.visitCharacterLiteral(this)
    }

}

/**
 * An implementation of [Literal] for string literals. Not to be confused with [JoinedStringExpression] which supports
 * interpolation within the string.
 *
 * Example:
 * ```
 * "Some string"
 * ```
 */
public class StringLiteral(value: String) : Literal<String>(value) {

    override fun <R> accept(visitor: AstVisitor<R>): R {
        return visitor.visitStringLiteral(this)
    }

}

/**
 * An implementation of [Literal] with a constant value of `-1` which is used to represent `null`.
 *
 * Example:
 * ```
 * null
 * ```
 */
// object because the value is always the same
public object NullLiteral : Literal<Int>(-1) {

    override fun <R> accept(visitor: AstVisitor<R>): R {
        return visitor.visitNullLiteral(this)
    }

}
