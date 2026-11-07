package me.filby.neptune.runescript.parser

import org.antlr.v4.runtime.BaseErrorListener
import org.antlr.v4.runtime.RecognitionException
import org.antlr.v4.runtime.Recognizer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TestScriptParserErrors {
    @Test
    fun testUnterminatedStringEndsAtLineBreak() {
        val errors = parseErrors("[proc,a]\nmes(\"open);\n[proc,b]\nreturn;\n")

        // reported at the opening quote, then the second script lexes normally: ')' and ';' were swallowed by the
        // string
        assertEquals(
            listOf(
                "2:4 unterminated string",
                "3:0 mismatched input '[' expecting {')', ','}",
            ),
            errors,
        )
    }

    @Test
    fun testUnterminatedStringEndsAtEndOfInput() {
        val errors = parseErrors("[proc,a]\nmes(\"open")

        assertEquals("2:4 unterminated string", errors.first())
    }

    @Test
    fun testUnterminatedStringFailsTheParse() {
        // the parser alone would accept this: the string ends at the line break and ';' completes the declaration
        val source = "[proc,a]\ndef_string \$s = \"abc\n;\n"

        val file = ScriptParser.createScriptFile(source, object : BaseErrorListener() {})

        assertNull(file)
    }

    /**
     * Parses [source] and returns each reported syntax error as `line:column message`.
     */
    private fun parseErrors(source: String): List<String> {
        val errors = mutableListOf<String>()
        val listener = object : BaseErrorListener() {
            override fun syntaxError(
                recognizer: Recognizer<*, *>?,
                offendingSymbol: Any?,
                line: Int,
                charPositionInLine: Int,
                msg: String?,
                e: RecognitionException?,
            ) {
                errors += "$line:$charPositionInLine $msg"
            }
        }
        ScriptParser.createScriptFile(source, listener)
        return errors
    }
}
