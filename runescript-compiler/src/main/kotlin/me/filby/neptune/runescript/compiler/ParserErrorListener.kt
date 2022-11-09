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
    private val lineOffset: Int = 0,
    private val columnOffset: Int = 0
) : BaseErrorListener() {
    override fun syntaxError(
        recognizer: Recognizer<*, *>,
        offendingSymbol: Any?,
        line: Int,
        charPositionInLine: Int,
        msg: String,
        e: RecognitionException?
    ) {
        // column offset only if we're on the first line since new line will reset the offset
        val columnOffset = if (line == 1) columnOffset else 0

        val realLine = line + lineOffset
        val realColumn = charPositionInLine + columnOffset + 1
        val source = NodeSourceLocation(sourceFile, realLine, realColumn)
        diagnostics.report(Diagnostic(DiagnosticType.SYNTAX_ERROR, source, msg.replace("%", "%%"), emptyList()))
    }
}
