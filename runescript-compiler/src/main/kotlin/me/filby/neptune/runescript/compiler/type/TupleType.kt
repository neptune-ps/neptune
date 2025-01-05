package me.filby.neptune.runescript.compiler.type

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

    override fun hashCode(): Int = children.hashCode()

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other !is TupleType) {
            return false
        }

        return children.contentEquals(other.children)
    }

    override fun toString(): String = MoreObjects.toStringHelper(this)
        .add("children", children)
        .toString()

    public companion object {
        /**
         * Converts a `List<Type>` into a singular [Type].
         *
         * - If the list is `null` or empty, [MetaType.Unit] is returned.
         * - If the list has a size of `1`, the first entry is returned.
         * - If the list has a size of over 1, a [TupleType] is returned with all types.
         */
        public fun fromList(types: List<Type>?): Type {
            if (types == null || types.isEmpty()) {
                return MetaType.Unit
            }
            if (types.size == 1) {
                return types.first()
            }
            return TupleType(*types.toTypedArray())
        }

        /**
         * Converts the [type] into a `List<Type>`.
         *
         * - If the [type] is a [TupleType], [TupleType.children] are returned as a list.
         * - If the [type] is `null`, [MetaType.Unit], or [MetaType.Nothing] an empty list is returned.
         * - If the [type] is a singular type, and is not `unit`, a list with just the [type] is returned.
         */
        // TODO move to a different location?
        public fun toList(type: Type?): List<Type> {
            if (type is TupleType) {
                // special case for tuples since we can convert the children into a list
                return type.children.toList()
            }
            if (type == null || type == MetaType.Unit || type == MetaType.Nothing) {
                // special case for null, unit, and nothing since it takes place of there being no types
                return emptyList()
            }
            // all other types just get wrapped in a list
            return listOf(type)
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
