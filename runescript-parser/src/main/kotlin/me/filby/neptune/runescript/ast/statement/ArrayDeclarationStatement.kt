package me.filby.neptune.runescript.ast.statement

import com.google.common.base.MoreObjects
import me.filby.neptune.runescript.ScriptVarType
import me.filby.neptune.runescript.ast.AstVisitor
import me.filby.neptune.runescript.ast.expr.Expression
import me.filby.neptune.runescript.ast.expr.Identifier
import java.util.*

/**
 * Represents a local array variable declaration with the given [type] and [name]. The [initializer] is what determines
 * the array size.
 *
 * Example:
 * ```
 * def_int $ints(50);
 * ```
 */
public class ArrayDeclarationStatement(
    public val type: ScriptVarType,
    public val name: Identifier,
    public val initializer: Expression
) : Statement() {

    init {
        addChild(name)
        addChild(initializer)
    }

    override fun <R> accept(visitor: AstVisitor<R>): R {
        return visitor.visitArrayDeclarationStatement(this)
    }

    override fun hashCode(): Int {
        return Objects.hash(type, name, initializer)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other !is ArrayDeclarationStatement) {
            return false
        }

        return type == other.type
            && name == other.name
            && initializer == other.initializer
    }

    override fun toString(): String {
        return MoreObjects.toStringHelper(this)
            .add("type", type)
            .add("name", name)
            .add("initializer", initializer)
            .toString()
    }

}
