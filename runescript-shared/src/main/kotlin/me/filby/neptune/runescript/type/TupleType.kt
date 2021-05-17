package me.filby.neptune.runescript.type

import com.google.common.base.MoreObjects

/**
 * A single type that combines multiple other types into one while still providing access to the other types.
 */
public class TupleType(vararg children: Type) : Type {

    /**
     * A flattened array of types this tuple contains.
     */
    public val children: Array<Type> = flatten(children)

    override val representation: String = this.children.joinToString(",") { it.representation }

    override val code: Char? = null

    override val baseType: BaseVarType? = null

    override val defaultValue: Any? = null

    override fun hashCode(): Int {
        return children.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other !is TupleType) {
            return false
        }

        return children.contentEquals(other.children)
    }

    override fun toString(): String {
        return MoreObjects.toStringHelper(this)
            .add("children", children)
            .toString()
    }

    private companion object {

        private fun flatten(types: Array<out Type>): Array<Type> {
            val flattened = mutableListOf<Type>()
            for (type in types) {
                if (type is TupleType) {
                    flattened += type.children
                } else {
                    flattened += type
                }
            }
            return flattened.toTypedArray()
        }

    }

}
