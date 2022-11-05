package me.filby.neptune.runescript.compiler.diagnostics

/**
 * Contains a list of [Diagnostics] reported during a compilation step.
 */
public class Diagnostics {
    /**
     * Private mutable list of [Diagnostic]s.
     */
    private val _diagnostics = mutableListOf<Diagnostic>()

    /**
     * Immutable list of [Diagnostic]s reported.
     *
     * @see report
     */
    public val diagnostics: List<Diagnostic> get() = _diagnostics

    /**
     * Adds the [diagnostic] to the list.
     */
    public fun report(diagnostic: Diagnostic) {
        _diagnostics += diagnostic
    }

    /**
     * Checks if any of the reported [Diagnostic]s have an error type.
     */
    public fun hasErrors(): Boolean = _diagnostics.any { it.type in ERROR_TYPES }

    private companion object {
        /**
         * All [DiagnosticType]s that count as an error during compilation.
         */
        private val ERROR_TYPES = setOf(DiagnosticType.ERROR, DiagnosticType.SYNTAX_ERROR)
    }
}
