package me.filby.neptune.runescript.ast

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * The base [abstract syntax tree](https://en.wikipedia.org/wiki/Abstract_syntax_tree) node.
 */
public abstract class Node {

    /**
     * A map of attributes that allows external code to add extra information to the node.
     */
    private val attributes = mutableMapOf<String, Any>()

    /**
     * Calls the node specific method on the [visitor].
     */
    public abstract fun <R> accept(visitor: AstVisitor<R>): R

    /**
     * Returns an attribute based on the given [key].
     */
    @Suppress("UNCHECKED_CAST")
    public fun <T : Any> getAttribute(key: String): T? {
        return attributes[key] as T?
    }

    /**
     * Adds (or replaces) an attribute with the given [key] with a value [value].
     */
    public fun <T : Any> putAttribute(key: String, value: T): T {
        attributes[key] = value
        return value
    }

    /**
     * Removes an attribute with the given [key].
     */
    public fun removeAttribute(key: String) {
        attributes.remove(key)
    }

    // mark abstract so all nodes have to implement
    public abstract override fun hashCode(): Int

    public abstract override fun equals(other: Any?): Boolean

    public abstract override fun toString(): String

    public companion object {

        /**
         * Returns a [ReadWriteProperty] for accessing attributes through delegation. If the attribute is not found an
         * error is thrown.
         */
        internal fun <N : Node, T : Any> attribute(key: String): ReadWriteProperty<N, T> =
            object : ReadWriteProperty<N, T> {

                @Suppress("UNCHECKED_CAST")
                override fun getValue(thisRef: N, property: KProperty<*>): T {
                    return thisRef.getAttribute(key)
                        ?: throw IllegalStateException("Property ${property.name} should be initialized before get.")
                }

                override fun setValue(thisRef: N, property: KProperty<*>, value: T) {
                    thisRef.putAttribute(key, value)
                }

            }

        /**
         * Returns a [ReadWriteProperty] for accessing attributes through delegation, if the attribute is not defined
         * the return value is `null` instead of throwing an error.
         */
        public fun <T : Any> attributeOrNull(key: String): ReadWriteProperty<Node, T?> =
            object : ReadWriteProperty<Node, T?> {

                @Suppress("UNCHECKED_CAST")
                override fun getValue(thisRef: Node, property: KProperty<*>): T? {
                    return thisRef.getAttribute(key) as T?
                }

                override fun setValue(thisRef: Node, property: KProperty<*>, value: T?) {
                    if (value == null) {
                        throw IllegalArgumentException("Property ${property.name} is not able to be set to null.")
                    }
                    thisRef.putAttribute(key, value)
                }

            }

    }

}
