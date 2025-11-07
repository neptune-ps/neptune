package me.filby.neptune.runescript.ast

import com.google.common.base.MoreObjects
import me.filby.neptune.runescript.ast.expr.Identifier
import me.filby.neptune.runescript.ast.statement.Statement
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
    source: NodeSourceLocation,
    public val trigger: Identifier,
    public val name: Identifier,
    public val isStar: Boolean,
    public val parameters: List<Parameter>?,
    public val returnTokens: List<Token>?,
    public val statements: List<Statement>,
) : Node(source) {
    public val nameString: String
        get() = if (isStar) "${name.text}*" else name.text

    init {
        addChild(trigger)
        addChild(name)
        if (parameters != null) {
            addChild(parameters)
        }
        if (returnTokens != null) {
            addChild(returnTokens)
        }
        addChild(statements)
    }

    override fun <R> accept(visitor: AstVisitor<R>): R = visitor.visitScript(this)

    override fun hashCode(): Int = Objects.hash(trigger, name, isStar, parameters, returnTokens, statements)

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other !is Script) {
            return false
        }

        return trigger == other.trigger &&
            name == other.name &&
            isStar == other.isStar &&
            parameters == other.parameters &&
            returnTokens == other.returnTokens &&
            statements == other.statements
    }

    override fun toString(): String = MoreObjects.toStringHelper(this)
        .add("trigger", trigger)
        .add("name", name)
        .add("isStar", isStar)
        .add("parameters", parameters)
        .add("returnTokens", returnTokens)
        .add("statements", statements)
        .toString()
}
