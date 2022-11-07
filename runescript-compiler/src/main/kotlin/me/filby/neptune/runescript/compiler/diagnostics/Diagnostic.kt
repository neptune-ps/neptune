package me.filby.neptune.runescript.compiler.diagnostics

import me.filby.neptune.runescript.ast.Node
import me.filby.neptune.runescript.ast.NodeSourceLocation

// TODO docs
public data class Diagnostic(
    public val type: DiagnosticType,
    public val sourceLocation: NodeSourceLocation,
    public val message: String,
    public val messageArgs: List<Any>
) {
    public constructor(
        type: DiagnosticType,
        node: Node,
        message: String,
        vararg messageArgs: Any
    ) : this(type, node.source, message, messageArgs.toList())

    public fun isError(): Boolean {
        return type == DiagnosticType.ERROR || type == DiagnosticType.SYNTAX_ERROR
    }
}
