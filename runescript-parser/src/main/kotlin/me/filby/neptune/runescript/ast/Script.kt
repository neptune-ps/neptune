package me.filby.neptune.runescript.ast

import com.google.common.base.MoreObjects
import me.filby.neptune.runescript.ast.expr.Identifier
import me.filby.neptune.runescript.ast.statement.Statement
import java.util.*

public class Script(
    public val trigger: Identifier,
    public val name: Identifier,
    public val statements: List<Statement>
) : Node() {

    override fun <R> accept(visitor: AstVisitor<R>): R {
        return visitor.visitScript(this)
    }

    override fun hashCode(): Int {
        return Objects.hash(trigger, name, statements)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true;
        }

        if (other !is Script) {
            return false
        }

        return trigger == other.trigger
            && name == other.name
            && statements == other.statements
    }

    override fun toString(): String {
        return MoreObjects.toStringHelper(this)
            .add("trigger", trigger)
            .add("name", name)
            .add("statements", statements)
            .toString()
    }

}
