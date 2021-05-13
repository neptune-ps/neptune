package me.filby.neptune.runescript.ast

/**
 * The base [abstract syntax tree](https://en.wikipedia.org/wiki/Abstract_syntax_tree) node.
 */
public abstract class Node {

    /**
     * Calls the node specific method on the [visitor].
     */
    public abstract fun <R> accept(visitor: AstVisitor<R>): R

    // mark abstract so all nodes have to implement
    public abstract override fun hashCode(): Int

    public abstract override fun equals(other: Any?): Boolean

    public abstract override fun toString(): String

}
