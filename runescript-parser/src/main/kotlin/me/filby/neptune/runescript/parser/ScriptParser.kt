package me.filby.neptune.runescript.parser

import me.filby.neptune.runescript.antlr.RuneScriptLexer
import me.filby.neptune.runescript.antlr.RuneScriptParser
import me.filby.neptune.runescript.ast.Node
import me.filby.neptune.runescript.ast.Script
import me.filby.neptune.runescript.ast.ScriptFile
import org.antlr.v4.runtime.BaseErrorListener
import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.RecognitionException
import org.antlr.v4.runtime.Recognizer

public object ScriptParser {

    private val ERROR_LISTENER = object : BaseErrorListener() {
        override fun syntaxError(
            recognizer: Recognizer<*, *>?,
            offendingSymbol: Any?,
            line: Int,
            charPositionInLine: Int,
            msg: String?,
            e: RecognitionException?
        ) {
            throw ParsingException(msg, e, line, charPositionInLine)
        }
    }

    public fun createScriptFile(scriptFile: String): ScriptFile {
        return invokeParser(CharStreams.fromString(scriptFile, "file"), RuneScriptParser::scriptFile) as ScriptFile
    }

    public fun createScript(script: String): Script {
        return invokeParser(CharStreams.fromString(script, "script"), RuneScriptParser::script) as Script
    }

    internal fun invokeParser(str: String, entry: (RuneScriptParser) -> ParserRuleContext): Node {
        return invokeParser(CharStreams.fromString(str), entry)
    }

    private fun invokeParser(stream: CharStream, entry: (RuneScriptParser) -> ParserRuleContext): Node {
        val lexer = RuneScriptLexer(stream)
        val tokens = CommonTokenStream(lexer)
        val parser = RuneScriptParser(tokens)

        lexer.removeErrorListeners()
        lexer.addErrorListener(ERROR_LISTENER)

        parser.removeErrorListeners()
        parser.addErrorListener(ERROR_LISTENER)

        val tree = entry.invoke(parser)

        return AstBuilder().visit(tree)
    }

}
