package me.filby.neptune.runescript.ast.expr

import com.google.common.base.MoreObjects
import me.filby.neptune.runescript.ast.AstVisitor
import me.filby.neptune.runescript.ast.NodeSourceLocation
import java.util.Objects

/**
 * An [Expression] that represents a constant value of [T].
 */
public sealed class Literal<T>(source: NodeSourceLocation, public val value: T) : Expression(source) {
    override fun hashCode(): Int = Objects.hashCode(value)

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other !is Literal<*>) {
            return false
        }

        return value == other.value
    }

    override fun toString(): String = MoreObjects.toStringHelper(this)
        .add("value", value)
        .toString()
}

/**
 * An implementation of [Literal] for numeric literals.
 *
 * Example:
 * ```
 * 123456
 * ```
 */
public class IntegerLiteral(source: NodeSourceLocation, value: String, public val radix: Int) :
    Literal<String>(source, value) {
    /**
     * The numeric value of this literal. If `null` the value hasn't been parsed yet.
     */
    public var numberValue: Number? = null

    override fun <R> accept(visitor: AstVisitor<R>): R = visitor.visitIntegerLiteral(this)

    override fun hashCode(): Int = Objects.hash(value, radix)

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other !is IntegerLiteral) {
            return false
        }

        return value == other.value && radix == other.radix
    }

    override fun toString(): String = MoreObjects.toStringHelper(this)
        .add("value", value)
        .add("radix", radix)
        .toString()

    public companion object {
        public const val RADIX_DECIMAL: Int = 10
        public const val RADIX_HEXADECIMAL: Int = 16
    }
}

/**
 * An implementation of [Literal] for coord literals.
 *
 * Example:
 * ```
 * 0_50_50_0_0
 * ```
 */
public class CoordLiteral(source: NodeSourceLocation, value: Int) : Literal<Int>(source, value) {
    override fun <R> accept(visitor: AstVisitor<R>): R = visitor.visitCoordLiteral(this)
}

/**
 * An implementation of [Literal] for boolean (`true`/`false`) literals.
 *
 * Example:
 * ```
 * true
 * ```
 */
public class BooleanLiteral(source: NodeSourceLocation, value: Boolean) : Literal<Boolean>(source, value) {
    override fun <R> accept(visitor: AstVisitor<R>): R = visitor.visitBooleanLiteral(this)
}

/**
 * An implementation of [Literal] for character literals.
 *
 * Example:
 * ```
 * 'c'
 * ```
 */
public class CharacterLiteral(source: NodeSourceLocation, value: Char) : Literal<Char>(source, value) {
    override fun <R> accept(visitor: AstVisitor<R>): R = visitor.visitCharacterLiteral(this)
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
public class StringLiteral(source: NodeSourceLocation, value: String) : Literal<String>(source, value) {
    override fun <R> accept(visitor: AstVisitor<R>): R = visitor.visitStringLiteral(this)
}

/**
 * An implementation of [Literal] with a constant value of `-1` which is used to represent `null`.
 *
 * Example:
 * ```
 * null
 * ```
 */
public class NullLiteral(source: NodeSourceLocation) : Literal<Int>(source, -1) {
    override fun <R> accept(visitor: AstVisitor<R>): R = visitor.visitNullLiteral(this)
}
