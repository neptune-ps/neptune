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

    init {
        assert(this.children.size >= 2) { "tuple type should not be used when type count is < 2" }
    }

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

    public companion object {
        /**
         * Converts a `List<Type>` into a singular [Type].
         *
         * - If the list is `null` or empty, `null` is returned.
         * - If the list has a size of `1`, the first entry is returned.
         * - If the list has a size of over 1, a [TupleType] is returned with all types.
         */
        public fun fromList(types: List<Type>?): Type? {
            if (types == null || types.isEmpty()) {
                return null
            }
            if (types.size == 1) {
                return types.first()
            }
            return TupleType(*types.toTypedArray())
        }

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
