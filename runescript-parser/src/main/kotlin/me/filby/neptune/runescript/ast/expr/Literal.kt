package me.filby.neptune.runescript.ast.expr

import com.google.common.base.MoreObjects
import me.filby.neptune.runescript.ast.AstVisitor
import me.filby.neptune.runescript.ast.Node
import java.util.*

// base literal class that has a constant value
public sealed class Literal<T>(public val value: T) : Expression() {

    override fun hashCode(): Int {
        return Objects.hashCode(value)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other !is Literal<*>) {
            return false;
        }

        return value == other.value
    }

    override fun toString(): String {
        return MoreObjects.toStringHelper(this)
            .add("value", value)
            .toString()
    }

}

public class IntegerLiteral(value: Int) : Literal<Int>(value) {

    override fun <R> accept(visitor: AstVisitor<R>): R {
        return visitor.visitIntegerLiteral(this)
    }

}

public class BooleanLiteral(value: Boolean) : Literal<Boolean>(value) {

    override fun <R> accept(visitor: AstVisitor<R>): R {
        return visitor.visitBooleanLiteral(this)
    }

}

public class CharacterLiteral(value: Char) : Literal<Char>(value) {

    override fun <R> accept(visitor: AstVisitor<R>): R {
        return visitor.visitCharacterLiteral(this)
    }

}

// object because the value is always the same
public object NullLiteral : Literal<Int>(-1) {

    override fun <R> accept(visitor: AstVisitor<R>): R {
        return visitor.visitNullLiteral(this)
    }

}
