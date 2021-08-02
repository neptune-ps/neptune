package me.filby.neptune.runescript.ast.statement

import com.google.common.base.MoreObjects
import me.filby.neptune.runescript.ast.AstVisitor
import me.filby.neptune.runescript.ast.expr.Expression
import me.filby.neptune.runescript.type.Type
import java.util.Objects

/**
 * Represents a switch statement for a given [type]. Switch statements contain a single [condition] (what to switch on)
 * and a list of [cases].
 *
 * Example:
 * ```
 * switch_int ($var) {
 *     case 1 : mes("matched 1");
 *     case 2 : mes("matched 2");
 *     case default : mes("unmatched: <tostring($var)>");
 * }
 * ```
 */
public class SwitchStatement(
    public val type: Type,
    public val condition: Expression,
    public val cases: List<SwitchCase>
) : Statement() {
    init {
        addChild(condition)
        addChild(cases)
    }

    override fun <R> accept(visitor: AstVisitor<R>): R {
        return visitor.visitSwitchStatement(this)
    }

    override fun hashCode(): Int {
        return Objects.hash(type, condition, cases)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other !is SwitchStatement) {
            return false
        }

        return type == other.type && condition == other.condition && cases == other.cases
    }

    override fun toString(): String {
        return MoreObjects.toStringHelper(this)
            .add("type", type)
            .add("condition", condition)
            .add("cases", cases)
            .toString()
    }
}
