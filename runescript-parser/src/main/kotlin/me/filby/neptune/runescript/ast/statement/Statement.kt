package me.filby.neptune.runescript.ast.statement

import me.filby.neptune.runescript.ast.Node
import me.filby.neptune.runescript.ast.NodeSourceLocation

/**
 * The base statement node that all statements extend.
 */
// base statement class
public abstract class Statement(source: NodeSourceLocation) : Node(source)
