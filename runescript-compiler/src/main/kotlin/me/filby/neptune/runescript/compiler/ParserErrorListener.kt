package me.filby.neptune.runescript.compiler

import me.filby.neptune.runescript.ast.NodeSourceLocation
import me.filby.neptune.runescript.compiler.diagnostics.Diagnostic
import me.filby.neptune.runescript.compiler.diagnostics.DiagnosticType
import me.filby.neptune.runescript.compiler.diagnostics.Diagnostics
import org.antlr.v4.runtime.BaseErrorListener
import org.antlr.v4.runtime.RecognitionException
import org.antlr.v4.runtime.Recognizer

/**
 * An antlr error listener that adds the error to [Diagnostics] for reporting later.
 */
internal class ParserErrorListener(
    private val sourceFile: String,
    private val diagnostics: Diagnostics,
) : BaseErrorListener() {
    override fun syntaxError(
        recognizer: Recognizer<*, *>,
        offendingSymbol: Any?,
        line: Int,
        charPositionInLine: Int,
        msg: String,
        e: RecognitionException?
    ) {
        val source = NodeSourceLocation(sourceFile, line, charPositionInLine + 1)
        diagnostics.report(Diagnostic(DiagnosticType.SYNTAX_ERROR, source, msg.replace("%", "%%"), emptyList()))
    }
}
