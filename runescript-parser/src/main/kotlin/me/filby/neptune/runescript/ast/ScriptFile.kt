package me.filby.neptune.runescript.ast

import com.google.common.base.MoreObjects
import java.util.*

public class ScriptFile(public val scripts: List<Script>) : Node() {

    override fun <R> accept(visitor: AstVisitor<R>): R {
        return visitor.visitScriptFile(this)
    }

    override fun hashCode(): Int {
        return Objects.hash(scripts)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other !is ScriptFile) {
            return false
        }

        return scripts == other.scripts
    }

    override fun toString(): String {
        return MoreObjects.toStringHelper(this)
            .add("scripts", scripts)
            .toString()
    }

}
