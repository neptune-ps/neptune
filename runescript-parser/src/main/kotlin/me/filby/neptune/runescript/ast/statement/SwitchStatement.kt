package me.filby.neptune.runescript.ast.statement

import com.google.common.base.MoreObjects
import me.filby.neptune.runescript.ScriptVarType
import me.filby.neptune.runescript.ast.AstVisitor
import me.filby.neptune.runescript.ast.expr.Expression
import java.util.*

public class SwitchStatement(
    public val type: ScriptVarType,
    public val condition: Expression,
    public val cases: List<SwitchCase>
) : Statement() {

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

        return type == other.type
            && condition == other.condition
            && cases == other.cases
    }

    override fun toString(): String {
        return MoreObjects.toStringHelper(this)
            .add("type", type)
            .add("condition", condition)
            .add("cases", cases)
            .toString()
    }

}
