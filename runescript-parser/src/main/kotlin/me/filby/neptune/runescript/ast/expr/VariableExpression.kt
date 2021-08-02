package me.filby.neptune.runescript.ast.expr

import com.google.common.base.MoreObjects
import me.filby.neptune.runescript.ast.AstVisitor
import java.util.Objects

/**
 * A base representation of a variable being used as an [Expression].
 */
// base class for a variable reference, all have an identifier
public sealed class VariableExpression(public val name: Identifier) : Expression() {
    init {
        addChild(name)
    }

    override fun toString(): String {
        return MoreObjects.toStringHelper(this)
            .add("name", name)
            .toString()
    }
}

/**
 * A [VariableExpression] implementation used for local variables within a script.
 *
 * Example:
 * ```
 * $var
 * ```
 */
public class LocalVariableExpression(
    name: Identifier,
    public val index: Expression? = null
) : VariableExpression(name) {
    init {
        addChild(index)
    }

    /**
     * Whether or not this variable expression references a local array variable.
     */
    public val isArray: Boolean get() = index != null

    override fun <R> accept(visitor: AstVisitor<R>): R {
        return visitor.visitLocalVariableExpression(this)
    }

    override fun hashCode(): Int {
        return Objects.hash(name, index)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other !is LocalVariableExpression) {
            return false
        }

        return name == other.name && index == other.index
    }

    override fun toString(): String {
        return MoreObjects.toStringHelper(this)
            .add("name", name)
            .add("index", index)
            .toString()
    }
}

/**
 * A [VariableExpression] implementation used for game variables within a script.
 *
 * Example:
 * ```
 * %var
 * ```
 */
public class GameVariableExpression(name: Identifier) : VariableExpression(name) {
    override fun <R> accept(visitor: AstVisitor<R>): R {
        return visitor.visitGameVariableExpression(this)
    }

    override fun hashCode(): Int {
        return Objects.hashCode(name)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other !is GameVariableExpression) {
            return false
        }

        return name == other.name
    }
}

/**
 * A [VariableExpression] implementation that represents a constant variable reference.
 *
 * Example:
 * ```
 * ^var
 * ```
 */
public class ConstantVariableExpression(name: Identifier) : VariableExpression(name) {
    override fun <R> accept(visitor: AstVisitor<R>): R {
        return visitor.visitConstantVariableExpression(this)
    }

    override fun hashCode(): Int {
        return Objects.hashCode(name)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other !is ConstantVariableExpression) {
            return false
        }

        return name == other.name
    }
}
