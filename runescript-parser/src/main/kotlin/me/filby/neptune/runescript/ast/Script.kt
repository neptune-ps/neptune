package me.filby.neptune.runescript.ast

import com.google.common.base.MoreObjects
import me.filby.neptune.runescript.ast.expr.Identifier
import me.filby.neptune.runescript.ast.statement.Statement
import me.filby.neptune.runescript.type.Type
import java.util.Objects

/**
 * A script declaration containing the header and code of a script.
 *
 * Example:
 * ```
 * [proc,minmax](int $min, int $max, int $value)
 * if ($max <= $min) {
 *     $min, $max = $max, $min;
 * }
 *
 * $value = ~min($max, $value);
 * $value = ~max($min, $value);
 * return($value);
 * ```
 */
public class Script(
    public val trigger: Identifier,
    public val name: Identifier,
    public val parameters: List<Parameter>?,
    public val returns: Type?,
    public val statements: List<Statement>
) : Node() {
    init {
        addChild(trigger)
        addChild(name)
        if (parameters != null) {
            addChild(parameters)
        }
        addChild(statements)
    }

    override fun <R> accept(visitor: AstVisitor<R>): R {
        return visitor.visitScript(this)
    }

    override fun hashCode(): Int {
        return Objects.hash(trigger, name, parameters, returns, statements)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other !is Script) {
            return false
        }

        return trigger == other.trigger && name == other.name && parameters == other.parameters &&
            returns == other.returns && statements == other.statements
    }

    override fun toString(): String {
        return MoreObjects.toStringHelper(this)
            .add("trigger", trigger)
            .add("name", name)
            .add("parameters", parameters)
            .add("returns", returns)
            .add("statements", statements)
            .toString()
    }
}
