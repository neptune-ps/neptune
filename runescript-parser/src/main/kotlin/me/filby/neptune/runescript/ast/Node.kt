package me.filby.neptune.runescript.ast

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * The base [abstract syntax tree](https://en.wikipedia.org/wiki/Abstract_syntax_tree) node.
 */
public abstract class Node(public val source: NodeSourceLocation) {
    /**
     * The nodes parent node if it belongs to one.
     */
    public var parent: Node? = null
        private set(value) {
            assert(field == null) { "parent already set" }
            field = value
        }

    /**
     * A [MutableList] of children for our use only.
     */
    private val _children = mutableListOf<Node>()

    /**
     * All nodes that belong (directly) to this node.
     */
    public val children: List<Node> get() = _children

    /**
     * A map of attributes that allows external code to add extra information to the node.
     */
    private val attributes = mutableMapOf<String, Any?>()

    /**
     * Calls the node specific method on the [visitor].
     */
    public abstract fun <R> accept(visitor: AstVisitor<R>): R

    /**
     * Adds [node] as a child of this node and sets its parent to this node.
     */
    protected fun addChild(node: Node?) {
        if (node == null) {
            return
        }

        node.parent = this
        _children += node
    }

    /**
     * Adds all [nodes] to this node as a child and sets their parent to this node.
     */
    protected fun addChild(nodes: List<Node?>) {
        for (node in nodes) {
            if (node == null) {
                continue
            }

            node.parent = this
            _children += node
        }
    }

    /**
     * Finds the first parent node by the given type recursively, or `null`.
     */
    public inline fun <reified T : Node> findParentByType(): T? {
        var curParent = parent
        while (curParent != null) {
            if (T::class.isInstance(curParent)) {
                return curParent as T
            }
            curParent = curParent.parent
        }
        return null
    }

    /**
     * Returns an attribute based on the given [key].
     */
    @Suppress("UNCHECKED_CAST")
    public fun <T> getAttribute(key: String): T? {
        return attributes[key] as T?
    }

    /**
     * Adds (or replaces) an attribute with the given [key] with a value [value].
     */
    public fun <T> putAttribute(key: String, value: T): T {
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
        public fun <T> attribute(key: String): ReadWriteProperty<Node, T> =
            object : ReadWriteProperty<Node, T> {
                @Suppress("UNCHECKED_CAST")
                override fun getValue(thisRef: Node, property: KProperty<*>): T {
                    if (thisRef.attributes.containsKey(key)) {
                        return thisRef.getAttribute<T>(key) as T
                    }
                    throw IllegalStateException("Property '${property.name}' should be initialized before get.")
                }

                override fun setValue(thisRef: Node, property: KProperty<*>, value: T) {
                    thisRef.putAttribute(key, value)
                }
            }

        /**
         * Returns a [ReadWriteProperty] for accessing attributes through delegation with support for an
         * initial value.
         */
        public fun <T> attribute(key: String, default: () -> T): ReadWriteProperty<Node, T> =
            object : ReadWriteProperty<Node, T> {
                @Suppress("UNCHECKED_CAST")
                override fun getValue(thisRef: Node, property: KProperty<*>): T {
                    return thisRef.getAttribute<T>(key) ?: thisRef.putAttribute(key, default())
                }

                override fun setValue(thisRef: Node, property: KProperty<*>, value: T) {
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
                    thisRef.putAttribute(key, value)
                }
            }
    }
}
