package me.filby.neptune.runescript.ast

/**
 * Represents the source location of a node in the source code.
 */
public data class NodeSourceLocation(
    val name: String,
    val line: Int,
    val column: Int
)
