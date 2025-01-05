package me.filby.neptune.runescript.parser

import me.filby.neptune.runescript.antlr.RuneScriptLexer
import me.filby.neptune.runescript.antlr.RuneScriptParser
import me.filby.neptune.runescript.ast.Node
import me.filby.neptune.runescript.ast.Script
import me.filby.neptune.runescript.ast.ScriptFile
import org.antlr.v4.runtime.ANTLRErrorListener
import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.ParserRuleContext
import java.nio.file.Path

public object ScriptParser {
    public fun createScriptFile(input: Path, errorListener: ANTLRErrorListener? = null): ScriptFile? {
        val absoluteNormalized = input.toAbsolutePath().normalize()
        return invokeParser(
            CharStreams.fromPath(absoluteNormalized),
            RuneScriptParser::scriptFile,
            errorListener,
        ) as? ScriptFile
    }

    public fun createScriptFile(scriptFile: String, errorListener: ANTLRErrorListener? = null): ScriptFile? =
        invokeParser(
            CharStreams.fromString(scriptFile, "<source>"),
            RuneScriptParser::scriptFile,
            errorListener,
        ) as? ScriptFile

    public fun createScript(script: String, errorListener: ANTLRErrorListener? = null): Script? = invokeParser(
        CharStreams.fromString(script, "<source>"),
        RuneScriptParser::script,
        errorListener,
    ) as? Script

    public fun invokeParser(
        stream: CharStream,
        entry: (RuneScriptParser) -> ParserRuleContext,
        errorListener: ANTLRErrorListener? = null,
        lineOffset: Int = 0,
        columnOffset: Int = 0,
    ): Node? {
        val lexer = RuneScriptLexer(stream)
        val tokens = CommonTokenStream(lexer)
        val parser = RuneScriptParser(tokens)

        // setup error listeners
        if (errorListener != null) {
            lexer.removeErrorListeners()
            lexer.addErrorListener(errorListener)

            parser.removeErrorListeners()
            parser.addErrorListener(errorListener)
        }

        val tree = entry(parser)

        // if there were any errors detected, return null for the whole node
        if (parser.numberOfSyntaxErrors > 0) {
            return null
        }

        return AstBuilder(stream.sourceName, lineOffset, columnOffset).visit(tree)
    }
}
