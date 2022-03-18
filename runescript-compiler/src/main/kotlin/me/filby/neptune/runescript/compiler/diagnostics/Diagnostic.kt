package me.filby.neptune.runescript.compiler.diagnostics

import me.filby.neptune.runescript.ast.Node

// TODO docs
public data class Diagnostic(
    public val type: DiagnosticType,
    public val node: Node,
    public val message: String,
    public val messageArgs: List<Any>
) {
    public constructor(
        type: DiagnosticType,
        node: Node,
        message: String,
        vararg messageArgs: Any
    ) : this(type, node, message, messageArgs.toList())
}
