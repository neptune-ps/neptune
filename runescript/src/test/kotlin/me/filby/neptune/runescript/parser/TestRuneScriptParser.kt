package me.filby.neptune.runescript.parser

import me.filby.neptune.runescript.antlr.RuneScriptParser
import me.filby.neptune.runescript.ast.Identifier
import me.filby.neptune.runescript.ast.Script
import me.filby.neptune.runescript.ast.expr.BooleanLiteral
import me.filby.neptune.runescript.ast.expr.IntegerLiteral
import me.filby.neptune.runescript.parser.ScriptParser.invokeParser
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals

class TestRuneScriptParser {

    @Test
    fun testIdentifier() {
        // normal identifier
        assertEquals(Identifier("abyssal_whip"),
            invokeParser("abyssal_whip", RuneScriptParser::identifier))

        // normal identifier beginning with digit
        assertEquals(Identifier("100_coral_flower"),
            invokeParser("100_coral_flower", RuneScriptParser::identifier))

        // component identifier (mostly)
        assertEquals(Identifier("smithing:arrowheads"),
            invokeParser("smithing:arrowheads", RuneScriptParser::identifier))
    }

    @Test
    fun testIntegerLiteral() {
        // integer literal
        assertEquals(IntegerLiteral(1337),
            invokeParser("1337", RuneScriptParser::literal))
        assertThrows<ParsingException>("line 1:0: mismatched input '1337_' expecting INTEGER_LITERAL") {
            invokeParser("1337_", RuneScriptParser::literal)
        }
    }

    @Test
    fun testBooleanLiteral() {
        assertEquals(BooleanLiteral(true),
            invokeParser("true", RuneScriptParser::literal))
        assertEquals(BooleanLiteral(false),
            invokeParser("false", RuneScriptParser::literal))
    }

    @Test
    fun testScript() {
        val script = ScriptParser.createScript("[opheld1,abyssal_whip]")
        val expected = Script(Identifier("opheld1"), Identifier("abyssal_whip"))
        assertEquals(expected, script)
    }

}
