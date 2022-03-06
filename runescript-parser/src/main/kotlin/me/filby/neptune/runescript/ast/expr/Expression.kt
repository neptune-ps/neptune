package me.filby.neptune.runescript.ast.expr

import me.filby.neptune.runescript.ast.Node
import me.filby.neptune.runescript.ast.NodeSourceLocation

/**
 * The base expression node that all expressions extend.
 */
public abstract class Expression(source: NodeSourceLocation) : Node(source)
