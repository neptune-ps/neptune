package me.filby.neptune.runescript.ast

import com.google.common.base.MoreObjects
import java.util.Objects

/**
 * The top level node type that represents a full file of [scripts].
 *
 * See [Script] for an example of what a script is.
 */
public class ScriptFile(source: NodeSourceLocation, public val scripts: List<Script>) : Node(source) {
    init {
        addChild(scripts)
    }

    override fun <R> accept(visitor: AstVisitor<R>): R = visitor.visitScriptFile(this)

    override fun hashCode(): Int = Objects.hash(scripts)

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other !is ScriptFile) {
            return false
        }

        return scripts == other.scripts
    }

    override fun toString(): String = MoreObjects.toStringHelper(this)
        .add("scripts", scripts)
        .toString()
}
