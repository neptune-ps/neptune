package me.filby.neptune.runescript.ast

import com.google.common.base.MoreObjects
import java.util.*

public class Script(
    public val trigger: Identifier,
    public val name: Identifier
) : Node() {

    override fun <R> accept(visitor: AstVisitor<R>): R {
        return visitor.visitScript(this)
    }

    override fun hashCode(): Int {
        return Objects.hash(trigger, name)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true;
        }

        if (other !is Script) {
            return false
        }

        return Objects.equals(trigger, other.trigger)
            && Objects.equals(name, other.name)
    }

    override fun toString(): String {
        return MoreObjects.toStringHelper(this)
            .add("trigger", trigger)
            .add("name", name)
            .toString()
    }

}
