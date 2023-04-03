package me.filby.neptune.runescript.compiler.configuration.command

import me.filby.neptune.runescript.ast.Node
import me.filby.neptune.runescript.ast.expr.CallExpression
import me.filby.neptune.runescript.ast.expr.Identifier
import me.filby.neptune.runescript.compiler.diagnostics.Diagnostic
import me.filby.neptune.runescript.compiler.diagnostics.DiagnosticType
import me.filby.neptune.runescript.compiler.diagnostics.Diagnostics

/**
 * A dynamic command handler allows more complex commands to be implemented.
 * Implementations are able to do custom type checking and code generations,
 * which allows for some commands to be implemented properly.
 *
 * A lot of implementations may not need to supply custom code generation,
 * which in that case they can omit [generateCode].
 */
public interface DynamicCommandHandler {
    /**
     * Handles type checking the expression. The expression will only ever be [CallExpression] or [Identifier].
     *
     * All implementations should follow these basic rules:
     *  - `expression.type` **must** be defined.
     *  - If `expression.symbol` is not defined, an attempt is made to look up a predefined symbol in the root
     *  table. If a predefined symbol wasn't found an internal compiler error will be thrown.
     *  - Errors should be reported using [Node.reportError].
     */
    public fun TypeCheckingContext.typeCheck()

    /**
     * Handles code generation for the command call.
     */
    public fun CodeGeneratorContext.generateCode() {
        expression.arguments.visit()
        command()
    }

    // helper functions

    /**
     * Helper function to report a diagnostic with the type of [DiagnosticType.INFO].
     */
    public fun Node.reportInfo(diagnostics: Diagnostics, message: String, vararg args: Any) {
        diagnostics.report(Diagnostic(DiagnosticType.INFO, this, message, *args))
    }

    /**
     * Helper function to report a diagnostic with the type of [DiagnosticType.WARNING].
     */
    public fun Node.reportWarning(diagnostics: Diagnostics, message: String, vararg args: Any) {
        diagnostics.report(Diagnostic(DiagnosticType.WARNING, this, message, *args))
    }

    /**
     * Helper function to report a diagnostic with the type of [DiagnosticType.ERROR].
     */
    public fun Node.reportError(diagnostics: Diagnostics, message: String, vararg args: Any) {
        diagnostics.report(Diagnostic(DiagnosticType.ERROR, this, message, *args))
    }
}
