package me.filby.neptune.runescript.compiler.diagnostics

// TODO documentation
public class Diagnostics {
    private val _diagnostics = mutableListOf<Diagnostic>()
    public val diagnostics: List<Diagnostic> get() = _diagnostics

    public fun report(diagnostic: Diagnostic) {
        _diagnostics += diagnostic
    }

    public fun hasErrors(): Boolean = _diagnostics.any { it.type == DiagnosticType.ERROR }
}
