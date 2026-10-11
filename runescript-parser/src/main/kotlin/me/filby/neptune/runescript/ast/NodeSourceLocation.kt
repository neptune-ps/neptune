package me.filby.neptune.runescript.ast

/**
 * Represents the source location of a node in the source code.
 *
 * [startOffset] and [endOffset] count code points in the parsed character stream. Unlike [line] and [column], they
 * aren't shifted by the parser's line and column offsets.
 */
public data class NodeSourceLocation @JvmOverloads constructor(
    val name: String,
    val line: Int,
    val column: Int,
    /**
     * The offset of the node's first character, or -1 if unknown.
     */
    val startOffset: Int = -1,
    /**
     * The offset just past the node's last character, or -1 if unknown.
     */
    val endOffset: Int = -1,
)
