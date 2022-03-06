package me.filby.neptune.runescript.ast.statement

import com.google.common.base.MoreObjects
import me.filby.neptune.runescript.ast.AstVisitor
import me.filby.neptune.runescript.ast.Node
import me.filby.neptune.runescript.ast.NodeSourceLocation
import me.filby.neptune.runescript.ast.expr.Expression
import java.util.Objects

/**
 * Represents a single [SwitchStatement] case. Contains the [keys] and the [statements] to run when the switch
 * statements condition matches one of the keys.
 *
 * See [SwitchStatement] for example.
 */
public class SwitchCase(
    source: NodeSourceLocation,
    public val keys: List<Expression>,
    public val statements: List<Statement>
) : Node(source) {
    init {
        addChild(keys)
        addChild(statements)
    }

    /**
     * Whether or not this switch case qualifies as the default case.
     */
    public val isDefault: Boolean get() = keys.isEmpty()

    override fun <R> accept(visitor: AstVisitor<R>): R {
        return visitor.visitSwitchCase(this)
    }

    override fun hashCode(): Int {
        return Objects.hash(keys, statements)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other !is SwitchCase) {
            return false
        }

        return keys == other.keys && statements == other.statements
    }

    override fun toString(): String {
        return MoreObjects.toStringHelper(this)
            .add("keys", keys)
            .add("statements", statements)
            .toString()
    }
}
